/**
 * Cloudflare Worker for Agora RTC Token Generation (Dark Group / CybeNode)
 * URL: https://group.cybenode-site.workers.dev
 *
 * Supported Endpoints:
 * 1. GET /token?channel=<channelName>&uid=<uid>
 * 2. GET /health
 * 3. OPTIONS /* (CORS Preflight)
 */

// Default Agora Credentials (can also be overridden in Cloudflare Environment Variables)
const DEFAULT_APP_ID = "38957a365fbe4faf80c1ebac552dd4b9";
const DEFAULT_APP_CERTIFICATE = "3b23110d30db45c9910b3c86928428fe";
const DEFAULT_APP_CERTIFICATE_SECONDARY = "6beaa95ad33d42afb812338b9b128a31";

const CORS_HEADERS = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
  "Access-Control-Allow-Headers": "Content-Type, Authorization",
};

export default {
  async fetch(request, env = {}, ctx) {
    // Handle CORS preflight
    if (request.method === "OPTIONS") {
      return new Response(null, { headers: CORS_HEADERS });
    }

    const url = new URL(request.url);
    const appId = env.AGORA_APP_ID || DEFAULT_APP_ID;
    const appCert = env.AGORA_APP_CERTIFICATE || DEFAULT_APP_CERTIFICATE;
    const secondaryCert = env.AGORA_APP_CERTIFICATE_SECONDARY || DEFAULT_APP_CERTIFICATE_SECONDARY;

    // Health Check Endpoint
    if (url.pathname === "/" || url.pathname === "/health") {
      return new Response(
        JSON.stringify({
          status: "online",
          service: "Dark Group RTC Token Server",
          version: "2.1.0",
          appId: appId.substring(0, 6) + "...",
          timestamp: Date.now(),
        }),
        {
          headers: { ...CORS_HEADERS, "Content-Type": "application/json" },
        }
      );
    }

    // Token Generation Endpoint: /token?channel=NAME&uid=12345
    if (url.pathname === "/token") {
      try {
        const channel = url.searchParams.get("channel") || "darkgroup_global";
        const uidParam = url.searchParams.get("uid") || "0";
        const certParam = url.searchParams.get("cert"); // optional: 'secondary'

        // Convert UID to 32-bit unsigned integer
        let uid = parseInt(uidParam, 10);
        if (isNaN(uid) || uid < 0) {
          uid = hashCode(uidParam);
        }

        // Validity period: 24 hours (86400 seconds)
        const expireDuration = 86400;
        const privilegeTs = Math.floor(Date.now() / 1000) + expireDuration;

        // Choose certificate
        const selectedCert = (certParam === "secondary") ? secondaryCert : appCert;

        const token = await generateAgoraRtcToken(
          appId,
          selectedCert,
          channel,
          uid,
          privilegeTs
        );

        return new Response(
          JSON.stringify({
            status: "secure",
            token: token,
            channel: channel,
            uid: uid,
            appId: appId,
            certificateUsed: certParam === "secondary" ? "secondary" : "primary",
            expiresIn: expireDuration,
            timestamp: Date.now(),
          }),
          {
            headers: { ...CORS_HEADERS, "Content-Type": "application/json" },
          }
        );
      } catch (error) {
        return new Response(
          JSON.stringify({
            status: "error",
            message: error.message || "Failed to generate Agora token",
            timestamp: Date.now(),
          }),
          {
            status: 500,
            headers: { ...CORS_HEADERS, "Content-Type": "application/json" },
          }
        );
      }
    }

    return new Response(
      JSON.stringify({ error: "Not Found", path: url.pathname }),
      {
        status: 404,
        headers: { ...CORS_HEADERS, "Content-Type": "application/json" },
      }
    );
  },
};

/**
 * Generate Agora RTC Token (v006) using Web Crypto API
 */
async function generateAgoraRtcToken(appId, appCertificate, channelName, uid, privilegeTs) {
  const salt = Math.floor(Math.random() * 99999999) + 1;
  const version = "006";

  // Build Privilege Message:
  // 1: kJoinChannel, 2: kPublishAudioStream
  const messageBytes = buildPrivilegeMessage(privilegeTs);

  // Payload for HMAC signing: appId + channelName + uid(4 LE) + salt(4 LE) + privilegeTs(4 LE) + messageBytes
  const enc = new TextEncoder();
  const appIdBytes = enc.encode(appId);
  const channelBytes = enc.encode(channelName);
  const uidBytes = packUint32(uid);
  const saltBytes = packUint32(salt);
  const privilegeTsBytes = packUint32(privilegeTs);

  const toSignLength =
    appIdBytes.length +
    channelBytes.length +
    uidBytes.length +
    saltBytes.length +
    privilegeTsBytes.length +
    messageBytes.length;

  const toSign = new Uint8Array(toSignLength);
  let offset = 0;
  toSign.set(appIdBytes, offset); offset += appIdBytes.length;
  toSign.set(channelBytes, offset); offset += channelBytes.length;
  toSign.set(uidBytes, offset); offset += uidBytes.length;
  toSign.set(saltBytes, offset); offset += saltBytes.length;
  toSign.set(privilegeTsBytes, offset); offset += privilegeTsBytes.length;
  toSign.set(messageBytes, offset);

  // HMAC-SHA256 signature
  const key = await crypto.subtle.importKey(
    "raw",
    enc.encode(appCertificate),
    { name: "HMAC", hash: "SHA-256" },
    false,
    ["sign"]
  );
  const signatureBuffer = await crypto.subtle.sign("HMAC", key, toSign);
  const signatureBytes = new Uint8Array(signatureBuffer);

  // CRCs
  const crcChannel = crc32(channelBytes);
  const crcUid = uid >>> 0;

  // Build packed message structure: salt + ts + packBytes(messageBytes)
  const innerPacked = new Uint8Array(4 + 4 + 2 + messageBytes.length);
  innerPacked.set(saltBytes, 0);
  innerPacked.set(privilegeTsBytes, 4);
  const dvInner = new DataView(innerPacked.buffer);
  dvInner.setUint16(8, messageBytes.length, true);
  innerPacked.set(messageBytes, 10);

  // Content Stream: signature + crcChannel + crcUid + packBytes(innerPacked)
  const contentLength = signatureBytes.length + 4 + 4 + 2 + innerPacked.length;
  const content = new Uint8Array(contentLength);
  let cOffset = 0;

  content.set(signatureBytes, cOffset); cOffset += signatureBytes.length;
  content.set(packUint32(crcChannel), cOffset); cOffset += 4;
  content.set(packUint32(crcUid), cOffset); cOffset += 4;

  const dvContent = new DataView(content.buffer);
  dvContent.setUint16(cOffset, innerPacked.length, true); cOffset += 2;
  content.set(innerPacked, cOffset);

  // Base64 encode
  const base64Content = bytesToBase64(content);
  return `${version}${appId}${base64Content}`;
}

function buildPrivilegeMessage(privilegeTs) {
  // short count (2) + [ short k (1) + uint32 ts (4) ] + [ short k (2) + uint32 ts (4) ] = 2 + 6 + 6 = 14 bytes
  const buf = new Uint8Array(14);
  const dv = new DataView(buf.buffer);
  dv.setUint16(0, 2, false); // Big-Endian short map size = 2

  // 1: kJoinChannel
  dv.setUint16(2, 1, false);
  dv.setUint32(4, privilegeTs >>> 0, true); // Little-Endian uint32

  // 2: kPublishAudioStream
  dv.setUint16(8, 2, false);
  dv.setUint32(10, privilegeTs >>> 0, true); // Little-Endian uint32

  return buf;
}

function packUint32(val) {
  const buf = new Uint8Array(4);
  buf[0] = val & 0xff;
  buf[1] = (val >>> 8) & 0xff;
  buf[2] = (val >>> 16) & 0xff;
  buf[3] = (val >>> 24) & 0xff;
  return buf;
}

function hashCode(str) {
  let hash = 0;
  for (let i = 0; i < str.length; i++) {
    hash = (Math.imul(31, hash) + str.charCodeAt(i)) | 0;
  }
  return Math.abs(hash) % 1000000;
}

// Fast CRC32 Implementation
const CRC_TABLE = new Int32Array(256);
for (let i = 0; i < 256; i++) {
  let c = i;
  for (let k = 0; k < 8; k++) {
    c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1;
  }
  CRC_TABLE[i] = c;
}

function crc32(bytes) {
  let crc = -1;
  for (let i = 0; i < bytes.length; i++) {
    crc = CRC_TABLE[(crc ^ bytes[i]) & 0xff] ^ (crc >>> 8);
  }
  return (crc ^ -1) >>> 0;
}

function bytesToBase64(bytes) {
  let binary = "";
  const len = bytes.byteLength;
  for (let i = 0; i < len; i++) {
    binary += String.fromCharCode(bytes[i]);
  }
  return btoa(binary);
}
