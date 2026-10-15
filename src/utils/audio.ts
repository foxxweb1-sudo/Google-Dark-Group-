class CallRingtoneManager {
  private audioCtx: AudioContext | null = null;
  private ringtoneInterval: number | null = null;
  private isPlaying = false;

  private initCtx() {
    if (!this.audioCtx) {
      const AudioContextClass = window.AudioContext || (window as unknown as { webkitAudioContext: typeof AudioContext }).webkitAudioContext;
      this.audioCtx = new AudioContextClass();
    }
    if (this.audioCtx.state === 'suspended') {
      this.audioCtx.resume();
    }
    return this.audioCtx;
  }

  startOutgoingRingtone() {
    this.stop();
    const ctx = this.initCtx();
    this.isPlaying = true;

    const playBurst = () => {
      if (!this.isPlaying || !this.audioCtx) return;
      try {
        const osc1 = this.audioCtx.createOscillator();
        const osc2 = this.audioCtx.createOscillator();
        const gain = this.audioCtx.createGain();

        osc1.type = 'sine';
        osc2.type = 'sine';
        osc1.frequency.setValueAtTime(440, this.audioCtx.currentTime);
        osc2.frequency.setValueAtTime(480, this.audioCtx.currentTime);

        gain.gain.setValueAtTime(0, this.audioCtx.currentTime);
        gain.gain.linearRampToValueAtTime(0.08, this.audioCtx.currentTime + 0.05);
        gain.gain.setValueAtTime(0.08, this.audioCtx.currentTime + 1.6);
        gain.gain.linearRampToValueAtTime(0, this.audioCtx.currentTime + 1.8);

        osc1.connect(gain);
        osc2.connect(gain);
        gain.connect(this.audioCtx.destination);

        osc1.start();
        osc2.start();
        osc1.stop(this.audioCtx.currentTime + 1.8);
        osc2.stop(this.audioCtx.currentTime + 1.8);
      } catch (e) {
        console.warn('Outgoing tone error:', e);
      }
    };

    playBurst();
    this.ringtoneInterval = window.setInterval(playBurst, 3500);
  }

  startIncomingRingtone() {
    this.stop();
    this.initCtx();
    this.isPlaying = true;

    const playChime = () => {
      if (!this.isPlaying || !this.audioCtx) return;
      try {
        const notes = [659.25, 830.61, 987.77, 1318.51, 987.77, 1318.51];
        const step = 0.14;

        notes.forEach((freq, idx) => {
          if (!this.audioCtx) return;
          const osc = this.audioCtx.createOscillator();
          const gain = this.audioCtx.createGain();
          const startTime = this.audioCtx.currentTime + idx * step;

          osc.type = 'triangle';
          osc.frequency.setValueAtTime(freq, startTime);

          gain.gain.setValueAtTime(0, startTime);
          gain.gain.linearRampToValueAtTime(0.12, startTime + 0.02);
          gain.gain.exponentialRampToValueAtTime(0.001, startTime + 0.28);

          osc.connect(gain);
          gain.connect(this.audioCtx.destination);

          osc.start(startTime);
          osc.stop(startTime + 0.3);
        });
      } catch (e) {
        console.warn('Incoming tone error:', e);
      }
    };

    playChime();
    this.ringtoneInterval = window.setInterval(playChime, 2400);
  }

  stop() {
    this.isPlaying = false;
    if (this.ringtoneInterval !== null) {
      clearInterval(this.ringtoneInterval);
      this.ringtoneInterval = null;
    }
  }
}

export const ringtoneManager = new CallRingtoneManager();

export class WebVoiceEngine {
  private mediaStream: MediaStream | null = null;
  private audioCtx: AudioContext | null = null;
  private analyser: AnalyserNode | null = null;
  private animFrameId: number | null = null;
  private simIntervalId: number | null = null;

  isConnected = false;
  isMuted = true;
  isDeafened = false;
  isSpeaking = false;
  soundWaveAmplitudes = [0.15, 0.15, 0.15];

  private onStateChangeCallback?: () => void;

  setOnStateChange(cb: () => void) {
    this.onStateChangeCallback = cb;
  }

  private notify() {
    if (this.onStateChangeCallback) {
      this.onStateChangeCallback();
    }
  }

  async connectVoice(requestRealMic = true) {
    this.isConnected = true;
    this.isMuted = false;
    this.isDeafened = false;

    if (requestRealMic && navigator.mediaDevices?.getUserMedia) {
      try {
        const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
        this.mediaStream = stream;
        const AudioContextClass = window.AudioContext || (window as unknown as { webkitAudioContext: typeof AudioContext }).webkitAudioContext;
        this.audioCtx = new AudioContextClass();
        const source = this.audioCtx.createMediaStreamSource(stream);
        this.analyser = this.audioCtx.createAnalyser();
        this.analyser.fftSize = 64;
        source.connect(this.analyser);

        this.startRealMicAnalysis();
        this.notify();
        return true;
      } catch (err) {
        console.warn('Microphone permission not granted or error, using simulation fallback:', err);
      }
    }

    this.startSimulatedAnalysis();
    this.notify();
    return true;
  }

  private startRealMicAnalysis() {
    const dataArray = new Uint8Array(32);
    const update = () => {
      if (!this.isConnected) return;
      if (this.isMuted || this.isDeafened) {
        this.isSpeaking = false;
        this.soundWaveAmplitudes = [0.15, 0.15, 0.15];
      } else if (this.analyser) {
        this.analyser.getByteFrequencyData(dataArray);
        let sum = 0;
        for (let i = 0; i < 16; i++) {
          sum += dataArray[i];
        }
        const avg = sum / 16;
        const normalized = Math.min(1, Math.max(0.15, avg / 120));
        this.isSpeaking = normalized > 0.28;

        this.soundWaveAmplitudes = [
          Math.min(1, (dataArray[2] || 20) / 140 + 0.15),
          Math.min(1, (dataArray[5] || 30) / 140 + 0.15),
          Math.min(1, (dataArray[8] || 20) / 140 + 0.15),
        ];
      }
      this.notify();
      this.animFrameId = requestAnimationFrame(update);
    };
    update();
  }

  private startSimulatedAnalysis() {
    if (this.simIntervalId) clearInterval(this.simIntervalId);
    this.simIntervalId = window.setInterval(() => {
      if (!this.isConnected || this.isMuted || this.isDeafened) {
        this.isSpeaking = false;
        this.soundWaveAmplitudes = [0.15, 0.15, 0.15];
      } else {
        const speakingChance = Math.random() > 0.4;
        this.isSpeaking = speakingChance;
        if (speakingChance) {
          this.soundWaveAmplitudes = [
            0.2 + Math.random() * 0.65,
            0.35 + Math.random() * 0.65,
            0.25 + Math.random() * 0.65,
          ];
        } else {
          this.soundWaveAmplitudes = [0.15, 0.2, 0.15];
        }
      }
      this.notify();
    }, 250);
  }

  disconnectVoice() {
    this.isConnected = false;
    this.isMuted = true;
    this.isDeafened = false;
    this.isSpeaking = false;
    this.soundWaveAmplitudes = [0.15, 0.15, 0.15];

    if (this.animFrameId) {
      cancelAnimationFrame(this.animFrameId);
      this.animFrameId = null;
    }
    if (this.simIntervalId) {
      clearInterval(this.simIntervalId);
      this.simIntervalId = null;
    }
    if (this.mediaStream) {
      this.mediaStream.getTracks().forEach((track) => track.stop());
      this.mediaStream = null;
    }
    if (this.audioCtx && this.audioCtx.state !== 'closed') {
      this.audioCtx.close().catch(() => {});
      this.audioCtx = null;
    }
    this.notify();
  }

  toggleMute(): boolean {
    if (!this.isConnected) return true;
    this.isMuted = !this.isMuted;
    if (this.mediaStream) {
      this.mediaStream.getAudioTracks().forEach((t) => {
        t.enabled = !this.isMuted;
      });
    }
    if (this.isMuted) {
      this.isSpeaking = false;
      this.soundWaveAmplitudes = [0.15, 0.15, 0.15];
    }
    this.notify();
    return this.isMuted;
  }

  toggleDeafen(): boolean {
    if (!this.isConnected) return false;
    this.isDeafened = !this.isDeafened;
    if (this.isDeafened) {
      this.isMuted = true;
      this.isSpeaking = false;
      this.soundWaveAmplitudes = [0.15, 0.15, 0.15];
    }
    this.notify();
    return this.isDeafened;
  }
}

export class VoiceRecorder {
  private mediaRecorder: MediaRecorder | null = null;
  private audioChunks: Blob[] = [];
  private startTime = 0;

  async start(): Promise<boolean> {
    try {
      this.audioChunks = [];
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      this.mediaRecorder = new MediaRecorder(stream);
      this.startTime = Date.now();

      this.mediaRecorder.ondataavailable = (event) => {
        if (event.data.size > 0) {
          this.audioChunks.push(event.data);
        }
      };

      this.mediaRecorder.start();
      return true;
    } catch (e) {
      console.warn('Voice memo mic denied, recording simulation supported:', e);
      return false;
    }
  }

  async stop(): Promise<{ blobUrl: string; durationSec: number }> {
    return new Promise((resolve) => {
      const fallbackDuration = Math.max(1, Math.round((Date.now() - this.startTime) / 1000));
      if (!this.mediaRecorder || this.mediaRecorder.state === 'inactive') {
        resolve({ blobUrl: '', durationSec: fallbackDuration });
        return;
      }

      this.mediaRecorder.onstop = () => {
        const audioBlob = new Blob(this.audioChunks, { type: 'audio/webm' });
        const blobUrl = URL.createObjectURL(audioBlob);
        const durationSec = Math.max(1, Math.round((Date.now() - this.startTime) / 1000));
        resolve({ blobUrl, durationSec });
      };

      this.mediaRecorder.stop();
      this.mediaRecorder.stream.getTracks().forEach((track) => track.stop());
    });
  }
}
