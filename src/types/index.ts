export interface User {
  id: string;
  username: string;
  name: string;
  avatarUrl?: string | null;
  isAnon?: boolean;
  password?: string;
  createdAt: number;
}

export interface RoomInfo {
  id: string;
  name: string;
  host: string;
  hostId: string;
  password?: string | null;
  isLocked: boolean;
  createdAt: number;
}

export interface RoomUser {
  id: string;
  peerId: string;
  name: string;
  username: string;
  avatarUrl?: string | null;
  isVoice: boolean;
  isMuted: boolean;
  isSpeaking: boolean;
}

export interface ChatMessage {
  id: string;
  roomCode: string;
  senderId: string;
  senderName: string;
  text: string;
  time: string;
  avatarUrl?: string | null;
  timestamp: number;
  isMe?: boolean;
  isSystem?: boolean;
}

export interface FriendRequest {
  id: string;
  fromUserId: string;
  fromUsername: string;
  fromName: string;
  fromAvatarUrl?: string | null;
  toUsername: string;
  timestamp: number;
}

export interface Friend {
  userId: string;
  username: string;
  name: string;
  avatarUrl?: string | null;
  isBlocked: boolean;
  isOnline: boolean;
  activeRoomCode?: string | null;
}

export interface PrivateConversation {
  id: string;
  otherUserId: string;
  otherUsername: string;
  otherName: string;
  otherAvatarUrl?: string | null;
  lastMessage: string;
  lastMessageTime: string;
  lastTimestamp: number;
  isBlocked: boolean;
}

export interface PrivateChatMessage {
  id: string;
  conversationId: string;
  senderId: string;
  senderName: string;
  text: string;
  isVoice: boolean;
  voiceDurationSec: number;
  time: string;
  timestamp: number;
  isMe?: boolean;
  deletedForEveryone?: boolean;
  deletedForUserIds?: string[];
  audioBlobUrl?: string;
}

export interface CallSession {
  id: string;
  callerId: string;
  callerName: string;
  callerUsername: string;
  callerAvatar?: string | null;
  receiverId: string;
  receiverName: string;
  receiverUsername: string;
  receiverAvatar?: string | null;
  channelId: string;
  status: 'ringing' | 'connected' | 'ended' | 'declined';
  timestamp: number;
}

export type CallUiState = 'IDLE' | 'OUTGOING_RINGING' | 'INCOMING_RINGING' | 'CONNECTED';

export type Screen =
  | 'SPLASH'
  | 'PERMISSIONS'
  | 'LANDING'
  | 'DASHBOARD'
  | 'ACTIVE_ROOM'
  | 'PROFILE'
  | 'FORUM'
  | 'PRIVACY'
  | 'FRIENDS'
  | 'PRIVATE_CHATS'
  | 'DIRECT_CHAT'
  | 'USER_PROFILE'
  | 'BROWSER';

export type ToastType = 'INFO' | 'SUCCESS' | 'ERROR';

export interface ToastEvent {
  id: string;
  message: string;
  type: ToastType;
}
