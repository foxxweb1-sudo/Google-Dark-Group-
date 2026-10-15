import React, { createContext, useContext, useState, useEffect, useRef, useCallback } from 'react';
import {
  User,
  RoomInfo,
  RoomUser,
  ChatMessage,
  Friend,
  FriendRequest,
  PrivateConversation,
  PrivateChatMessage,
  CallSession,
  CallUiState,
  Screen,
  ToastEvent,
  ToastType,
} from '../types';
import { dataService } from '../services/dataService';
import { WebVoiceEngine, ringtoneManager } from '../utils/audio';

interface AppContextType {
  currentScreen: Screen;
  navigateTo: (screen: Screen) => void;

  currentUser: User | null;
  register: (name: string, username: string, pass: string, isAnon: boolean, avatarUrl?: string | null) => Promise<boolean>;
  login: (username: string, pass: string) => Promise<boolean>;
  logout: () => void;
  updateProfile: (name: string, pass?: string, avatarUrl?: string | null) => Promise<boolean>;

  toasts: ToastEvent[];
  showToast: (message: string, type?: ToastType) => void;
  dismissToast: (id: string) => void;

  isVoiceConnected: boolean;
  isVoiceMuted: boolean;
  isVoiceDeafened: boolean;
  isVoiceSpeaking: boolean;
  soundWaveAmplitudes: number[];
  toggleVoiceConnection: () => Promise<void>;
  toggleVoiceMute: () => void;
  toggleVoiceDeafen: () => void;

  allRooms: RoomInfo[];
  mySavedRooms: RoomInfo[];
  friendsRooms: RoomInfo[];
  currentRoom: RoomInfo | null;
  roomUsers: RoomUser[];
  roomMessages: ChatMessage[];
  createRoom: (name: string, pass?: string | null) => Promise<RoomInfo | null>;
  joinRoom: (roomCode: string, passAttempt?: string) => Promise<boolean>;
  leaveRoom: () => Promise<void>;
  deleteCurrentRoom: () => Promise<boolean>;
  sendRoomMessage: (text: string) => Promise<boolean>;
  refreshRooms: () => Promise<void>;

  forumMessages: ChatMessage[];
  sendForumMessage: (text: string) => Promise<boolean>;
  refreshForum: () => Promise<void>;

  friends: Friend[];
  friendRequests: FriendRequest[];
  searchUserResult: User | null;
  isSearchingUser: boolean;
  searchUser: (username: string) => Promise<User | null>;
  sendFriendRequest: (toUsername: string) => Promise<boolean>;
  acceptFriendRequest: (req: FriendRequest) => Promise<boolean>;
  declineFriendRequest: (reqId: string) => Promise<boolean>;
  toggleBlockFriend: (friendUserId: string) => Promise<boolean>;
  removeFriend: (friendUserId: string) => Promise<boolean>;
  refreshFriends: () => Promise<void>;

  selectedUserProfile: User | null;
  viewUserProfile: (user: User) => void;

  conversations: PrivateConversation[];
  activeDirectUser: User | null;
  openDirectChat: (targetUser: User) => Promise<void>;
  privateMessages: PrivateChatMessage[];
  sendPrivateTextMessage: (text: string) => Promise<boolean>;
  sendPrivateVoiceMessage: (blobUrl: string, durationSec: number) => Promise<boolean>;
  deletePrivateMessage: (msgId: string, forEveryone: boolean) => Promise<boolean>;
  refreshConversations: () => Promise<void>;

  callUiState: CallUiState;
  activeCallSession: CallSession | null;
  callDurationSec: number;
  startCall: (targetUser: User) => Promise<void>;
  answerCall: () => Promise<void>;
  declineCall: () => Promise<void>;
  endCall: () => Promise<void>;

  browserUrl: string;
  browserTitle: string;
  openInAppBrowser: (url?: string, title?: string) => void;
  closeInAppBrowser: () => void;

  downloadUrl: string;
  supportUrl: string;
  privacyUrl: string;
}

const AppContext = createContext<AppContextType | null>(null);

export const AppProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [currentScreen, setCurrentScreen] = useState<Screen>('SPLASH');
  const [previousScreenBeforeBrowser, setPreviousScreenBeforeBrowser] = useState<Screen>('DASHBOARD');
  const [currentUser, setCurrentUser] = useState<User | null>(() => dataService.getCurrentUser());

  const [toasts, setToasts] = useState<ToastEvent[]>([]);

  const voiceEngineRef = useRef<WebVoiceEngine>(new WebVoiceEngine());
  const [isVoiceConnected, setIsVoiceConnected] = useState(false);
  const [isVoiceMuted, setIsVoiceMuted] = useState(true);
  const [isVoiceDeafened, setIsVoiceDeafened] = useState(false);
  const [isVoiceSpeaking, setIsVoiceSpeaking] = useState(false);
  const [soundWaveAmplitudes, setSoundWaveAmplitudes] = useState([0.15, 0.15, 0.15]);

  const [allRooms, setAllRooms] = useState<RoomInfo[]>([]);
  const [currentRoom, setCurrentRoom] = useState<RoomInfo | null>(null);
  const [roomUsers, setRoomUsers] = useState<RoomUser[]>([]);
  const [roomMessages, setRoomMessages] = useState<ChatMessage[]>([]);

  const [forumMessages, setForumMessages] = useState<ChatMessage[]>([]);

  const [friends, setFriends] = useState<Friend[]>([]);
  const [friendRequests, setFriendRequests] = useState<FriendRequest[]>([]);
  const [searchUserResult, setSearchUserResult] = useState<User | null>(null);
  const [isSearchingUser, setIsSearchingUser] = useState(false);
  const [selectedUserProfile, setSelectedUserProfile] = useState<User | null>(null);

  const [conversations, setConversations] = useState<PrivateConversation[]>([]);
  const [activeDirectUser, setActiveDirectUser] = useState<User | null>(null);
  const [privateMessages, setPrivateMessages] = useState<PrivateChatMessage[]>([]);

  const [callUiState, setCallUiState] = useState<CallUiState>('IDLE');
  const [activeCallSession, setActiveCallSession] = useState<CallSession | null>(null);
  const [callDurationSec, setCallDurationSec] = useState(0);
  const callTimerRef = useRef<number | null>(null);

  const [browserUrl, setBrowserUrl] = useState('https://owner.cybenode.site/');
  const [browserTitle, setBrowserTitle] = useState('معلومات المطور');

  const downloadUrl = dataService.getDownloadUrl();
  const supportUrl = dataService.getSupportUrl();
  const privacyUrl = dataService.getPrivacyPolicyUrl();

  const showToast = useCallback((message: string, type: ToastType = 'INFO') => {
    const id = 'toast_' + Math.random().toString(36).substring(2, 9);
    setToasts((prev) => [...prev, { id, message, type }]);
    setTimeout(() => {
      setToasts((prev) => prev.filter((t) => t.id !== id));
    }, 3800);
  }, []);

  const dismissToast = useCallback((id: string) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  useEffect(() => {
    const ve = voiceEngineRef.current;
    ve.setOnStateChange(() => {
      setIsVoiceConnected(ve.isConnected);
      setIsVoiceMuted(ve.isMuted);
      setIsVoiceDeafened(ve.isDeafened);
      setIsVoiceSpeaking(ve.isSpeaking);
      setSoundWaveAmplitudes([...ve.soundWaveAmplitudes]);
    });

    return () => {
      ve.disconnectVoice();
    };
  }, []);

  const refreshRooms = useCallback(async () => {
    const rms = await dataService.fetchAllRooms();
    setAllRooms(rms);
  }, []);

  const refreshForum = useCallback(async () => {
    const msgs = await dataService.fetchForumMessages();
    setForumMessages(msgs);
  }, []);

  const refreshFriends = useCallback(async () => {
    if (!currentUser) return;
    const fList = await dataService.fetchFriends(currentUser.id);
    setFriends(fList);
    const rList = await dataService.fetchFriendRequests(currentUser.username);
    setFriendRequests(rList);
  }, [currentUser]);

  const refreshConversations = useCallback(async () => {
    if (!currentUser) return;
    const convs = await dataService.fetchConversations(currentUser.id);
    setConversations(convs);
  }, [currentUser]);

  useEffect(() => {
    refreshRooms();
    refreshForum();
  }, [refreshRooms, refreshForum]);

  useEffect(() => {
    if (currentUser) {
      refreshFriends();
      refreshConversations();
    }
  }, [currentUser, refreshFriends, refreshConversations]);

  useEffect(() => {
    if (!currentUser || callUiState !== 'IDLE') return;
    const interval = setInterval(() => {
      const inc = dataService.checkIncomingCall(currentUser.id);
      if (inc && inc.status === 'ringing') {
        setActiveCallSession(inc);
        setCallUiState('INCOMING_RINGING');
        ringtoneManager.startIncomingRingtone();
      }
    }, 2500);
    return () => clearInterval(interval);
  }, [currentUser, callUiState]);

  useEffect(() => {
    if (callUiState === 'CONNECTED') {
      callTimerRef.current = window.setInterval(() => {
        setCallDurationSec((sec) => sec + 1);
      }, 1000);
    } else {
      if (callTimerRef.current) {
        clearInterval(callTimerRef.current);
        callTimerRef.current = null;
      }
      setCallDurationSec(0);
    }
    return () => {
      if (callTimerRef.current) clearInterval(callTimerRef.current);
    };
  }, [callUiState]);

  const register = async (name: string, rawUsername: string, pass: string, isAnon: boolean, avatarUrl?: string | null): Promise<boolean> => {
    const username = rawUsername.toLowerCase().replace(/[^a-z0-9_]/g, '');
    if (!username) {
      showToast('اسم المستخدم غير صالح، استخدم حروف وأرقام إنجليزية فقط', 'ERROR');
      return false;
    }

    const existing = await dataService.getUser(username);
    if (existing) {
      showToast('اسم المستخدم محجوز بالفعل! جرب اسماً آخر.', 'ERROR');
      return false;
    }

    const newUser: User = {
      id: 'usr_' + Math.floor(100000 + Math.random() * 900000),
      username,
      name: name.trim() || 'مستخدم Dark Group',
      avatarUrl: avatarUrl || null,
      isAnon,
      password: pass,
      createdAt: Date.now(),
    };

    await dataService.saveUser(newUser);
    dataService.setCurrentUser(newUser);
    setCurrentUser(newUser);
    setCurrentScreen('DASHBOARD');
    showToast('تم إنشاء الحساب بنجاح في Dark Group!', 'SUCCESS');
    return true;
  };

  const login = async (rawUsername: string, pass: string): Promise<boolean> => {
    const username = rawUsername.toLowerCase().replace(/[^a-z0-9_]/g, '');
    const user = await dataService.getUser(username);
    if (!user) {
      showToast('المستخدم غير موجود. تأكد من الاسم أو قم بإنشاء حساب جديد.', 'ERROR');
      return false;
    }

    if (user.password && user.password !== pass) {
      showToast('كلمة المرور غير صحيحة!', 'ERROR');
      return false;
    }

    dataService.setCurrentUser(user);
    setCurrentUser(user);
    setCurrentScreen('DASHBOARD');
    showToast(`مرحباً بعودتك ${user.name}!`, 'SUCCESS');
    return true;
  };

  const logout = () => {
    voiceEngineRef.current.disconnectVoice();
    dataService.setCurrentUser(null);
    setCurrentUser(null);
    setCurrentRoom(null);
    setActiveDirectUser(null);
    setCallUiState('IDLE');
    ringtoneManager.stop();
    setCurrentScreen('LANDING');
    showToast('تم تسجيل الخروج بنجاح', 'INFO');
  };

  const updateProfile = async (name: string, pass?: string, avatarUrl?: string | null): Promise<boolean> => {
    if (!currentUser) return false;
    const ok = await dataService.updateUser(currentUser.username, name, pass, avatarUrl);
    if (ok) {
      const updated: User = {
        ...currentUser,
        name,
        ...(pass ? { password: pass } : {}),
        ...(avatarUrl !== undefined ? { avatarUrl } : {}),
      };
      setCurrentUser(updated);
      showToast('تم تحديث الملف الشخصي بنجاح', 'SUCCESS');
    }
    return ok;
  };

  const navigateTo = (screen: Screen) => {
    setCurrentScreen(screen);
  };

  const toggleVoiceConnection = async () => {
    const ve = voiceEngineRef.current;
    if (ve.isConnected) {
      ve.disconnectVoice();
      if (currentRoom && currentUser) {
        await dataService.updateVoiceState(currentRoom.id, currentUser.id, false, true, false);
        const users = await dataService.fetchRoomUsers(currentRoom.id);
        setRoomUsers(users);
      }
      showToast('تم قطع الاتصال الصوتي', 'INFO');
    } else {
      await ve.connectVoice(true);
      if (currentRoom && currentUser) {
        await dataService.updateVoiceState(currentRoom.id, currentUser.id, true, ve.isMuted, ve.isSpeaking);
        const users = await dataService.fetchRoomUsers(currentRoom.id);
        setRoomUsers(users);
      }
      showToast('تم الاتصال بالقناة الصوتية المشفرة', 'SUCCESS');
    }
  };

  const toggleVoiceMute = async () => {
    const ve = voiceEngineRef.current;
    const isNowMuted = ve.toggleMute();
    if (currentRoom && currentUser) {
      await dataService.updateVoiceState(currentRoom.id, currentUser.id, ve.isConnected, isNowMuted, false);
      const users = await dataService.fetchRoomUsers(currentRoom.id);
      setRoomUsers(users);
    }
  };

  const toggleVoiceDeafen = () => {
    const ve = voiceEngineRef.current;
    ve.toggleDeafen();
  };

  const createRoom = async (name: string, pass?: string | null): Promise<RoomInfo | null> => {
    if (!currentUser) return null;
    const code = 'NODE-' + Math.floor(1000 + Math.random() * 9000);
    const newRoom: RoomInfo = {
      id: code,
      name: name.trim() || `غرفة ${code}`,
      host: currentUser.name,
      hostId: currentUser.id,
      password: pass && pass.trim().length > 0 ? pass.trim() : null,
      isLocked: !!(pass && pass.trim().length > 0),
      createdAt: Date.now(),
    };

    await dataService.createRoom(newRoom);
    await refreshRooms();
    await joinRoom(code, pass || undefined);
    return newRoom;
  };

  const joinRoom = async (roomCode: string, passAttempt?: string): Promise<boolean> => {
    if (!currentUser) return false;
    const room = await dataService.getRoom(roomCode);
    if (!room) {
      showToast('الغرفة المطلوبة غير موجودة!', 'ERROR');
      return false;
    }

    if (room.isLocked && room.password) {
      if (room.hostId !== currentUser.id && room.password !== passAttempt) {
        showToast('كلمة مرور الغرفة غير صحيحة!', 'ERROR');
        return false;
      }
    }

    setCurrentRoom(room);
    const roomUser: RoomUser = {
      id: currentUser.id,
      peerId: `peer_${currentUser.id}`,
      name: currentUser.name,
      username: currentUser.username,
      avatarUrl: currentUser.avatarUrl,
      isVoice: false,
      isMuted: true,
      isSpeaking: false,
    };

    await dataService.joinRoomUser(room.id, roomUser);
    const users = await dataService.fetchRoomUsers(room.id);
    setRoomUsers(users);

    const msgs = await dataService.fetchRoomMessages(room.id);
    setRoomMessages(msgs);

    setCurrentScreen('ACTIVE_ROOM');
    showToast(`تم الانضمام إلى: ${room.name}`, 'SUCCESS');
    return true;
  };

  const leaveRoom = async () => {
    voiceEngineRef.current.disconnectVoice();
    if (currentRoom && currentUser) {
      await dataService.leaveRoomUser(currentRoom.id, currentUser.id);
    }
    setCurrentRoom(null);
    setCurrentScreen('DASHBOARD');
  };

  const deleteCurrentRoom = async (): Promise<boolean> => {
    if (!currentRoom || !currentUser) return false;
    if (currentRoom.hostId !== currentUser.id) {
      showToast('أنت لست منشئ هذه الغرفة!', 'ERROR');
      return false;
    }
    await dataService.deleteRoom(currentRoom.id);
    voiceEngineRef.current.disconnectVoice();
    setCurrentRoom(null);
    await refreshRooms();
    setCurrentScreen('DASHBOARD');
    showToast('تم حذف الغرفة بنجاح', 'SUCCESS');
    return true;
  };

  const sendRoomMessage = async (text: string): Promise<boolean> => {
    if (!currentRoom || !currentUser || !text.trim()) return false;
    const msg: ChatMessage = {
      id: 'rmsg_' + Math.random().toString(36).substring(2, 9),
      roomCode: currentRoom.id,
      senderId: currentUser.id,
      senderName: currentUser.name,
      text: text.trim(),
      time: new Date().toLocaleTimeString('ar-EG', { hour: '2-digit', minute: '2-digit' }),
      avatarUrl: currentUser.avatarUrl,
      timestamp: Date.now(),
      isMe: true,
      isSystem: false,
    };
    await dataService.sendRoomMessage(currentRoom.id, msg);
    setRoomMessages((prev) => [...prev, msg]);
    return true;
  };

  const sendForumMessage = async (text: string): Promise<boolean> => {
    if (!currentUser || !text.trim()) return false;
    const msg: ChatMessage = {
      id: 'fmsg_' + Math.random().toString(36).substring(2, 9),
      roomCode: 'FORUM',
      senderId: currentUser.id,
      senderName: currentUser.name,
      text: text.trim(),
      time: new Date().toLocaleTimeString('ar-EG', { hour: '2-digit', minute: '2-digit' }),
      avatarUrl: currentUser.avatarUrl,
      timestamp: Date.now(),
      isMe: true,
      isSystem: false,
    };
    await dataService.sendForumMessage(msg);
    setForumMessages((prev) => [...prev, msg]);
    showToast('تم نشر المشاركة في المنتدى العام', 'SUCCESS');
    return true;
  };

  const searchUser = async (username: string): Promise<User | null> => {
    setIsSearchingUser(true);
    const res = await dataService.getUser(username);
    setIsSearchingUser(false);
    setSearchUserResult(res);
    return res;
  };

  const sendFriendRequest = async (toUsername: string): Promise<boolean> => {
    if (!currentUser) return false;
    const clean = toUsername.toLowerCase().trim();
    if (clean === currentUser.username.toLowerCase()) {
      showToast('لا يمكنك إرسال طلب صداقة لنفسك!', 'ERROR');
      return false;
    }

    const target = await dataService.getUser(clean);
    if (!target) {
      showToast('المستخدم غير موجود!', 'ERROR');
      return false;
    }

    const req: FriendRequest = {
      id: 'req_' + Math.random().toString(36).substring(2, 9),
      fromUserId: currentUser.id,
      fromUsername: currentUser.username,
      fromName: currentUser.name,
      fromAvatarUrl: currentUser.avatarUrl,
      toUsername: clean,
      timestamp: Date.now(),
    };

    await dataService.sendFriendRequest(req);
    showToast(`تم إرسال طلب الصداقة إلى @${clean}`, 'SUCCESS');
    return true;
  };

  const acceptFriendRequest = async (req: FriendRequest): Promise<boolean> => {
    if (!currentUser) return false;
    await dataService.acceptFriendRequest(req, currentUser.id);
    await refreshFriends();
    showToast(`أصبح @${req.fromUsername} صديقاً لك الآن!`, 'SUCCESS');
    return true;
  };

  const declineFriendRequest = async (reqId: string): Promise<boolean> => {
    if (!currentUser) return false;
    await dataService.declineFriendRequest(reqId, currentUser.username);
    await refreshFriends();
    showToast('تم رفض طلب الصداقة', 'INFO');
    return true;
  };

  const toggleBlockFriend = async (friendUserId: string): Promise<boolean> => {
    if (!currentUser) return false;
    const currentFriend = friends.find((f: Friend) => f.userId === friendUserId);
    const newBlocked = !currentFriend?.isBlocked;
    await dataService.blockFriend(currentUser.id, friendUserId, newBlocked);
    await refreshFriends();
    showToast(newBlocked ? 'تم حظر المستخدم' : 'تم إلغاء حظر المستخدم', 'INFO');
    return true;
  };

  const removeFriend = async (friendUserId: string): Promise<boolean> => {
    if (!currentUser) return false;
    await dataService.removeFriend(currentUser.id, friendUserId);
    await refreshFriends();
    showToast('تمت إزالة الصديق', 'INFO');
    return true;
  };

  const viewUserProfile = (user: User) => {
    setSelectedUserProfile(user);
    setCurrentScreen('USER_PROFILE');
  };

  const openDirectChat = async (targetUser: User) => {
    if (!currentUser) return;
    setActiveDirectUser(targetUser);
    const convId = `conv_${[currentUser.id, targetUser.id].sort().join('_')}`;
    const msgs = await dataService.fetchPrivateMessages(convId, currentUser.id);
    setPrivateMessages(msgs);
    setCurrentScreen('DIRECT_CHAT');
  };

  const sendPrivateTextMessage = async (text: string): Promise<boolean> => {
    if (!currentUser || !activeDirectUser || !text.trim()) return false;
    const convId = `conv_${[currentUser.id, activeDirectUser.id].sort().join('_')}`;
    const msg: PrivateChatMessage = {
      id: 'pmsg_' + Math.random().toString(36).substring(2, 9),
      conversationId: convId,
      senderId: currentUser.id,
      senderName: currentUser.name,
      text: text.trim(),
      isVoice: false,
      voiceDurationSec: 0,
      time: new Date().toLocaleTimeString('ar-EG', { hour: '2-digit', minute: '2-digit' }),
      timestamp: Date.now(),
      isMe: true,
      deletedForEveryone: false,
      deletedForUserIds: [],
    };

    await dataService.sendPrivateMessage(msg, activeDirectUser, currentUser);
    setPrivateMessages((prev) => [...prev, msg]);
    await refreshConversations();
    return true;
  };

  const sendPrivateVoiceMessage = async (blobUrl: string, durationSec: number): Promise<boolean> => {
    if (!currentUser || !activeDirectUser) return false;
    const convId = `conv_${[currentUser.id, activeDirectUser.id].sort().join('_')}`;
    const msg: PrivateChatMessage = {
      id: 'pmsg_' + Math.random().toString(36).substring(2, 9),
      conversationId: convId,
      senderId: currentUser.id,
      senderName: currentUser.name,
      text: 'تسجيل صوتي مشفر',
      isVoice: true,
      voiceDurationSec: durationSec,
      time: new Date().toLocaleTimeString('ar-EG', { hour: '2-digit', minute: '2-digit' }),
      timestamp: Date.now(),
      isMe: true,
      deletedForEveryone: false,
      deletedForUserIds: [],
      audioBlobUrl: blobUrl,
    };

    await dataService.sendPrivateMessage(msg, activeDirectUser, currentUser);
    setPrivateMessages((prev) => [...prev, msg]);
    await refreshConversations();
    showToast('تم إرسال الرسالة الصوتية المشفرة', 'SUCCESS');
    return true;
  };

  const deletePrivateMessage = async (msgId: string, forEveryone: boolean): Promise<boolean> => {
    if (!currentUser || !activeDirectUser) return false;
    const convId = `conv_${[currentUser.id, activeDirectUser.id].sort().join('_')}`;
    await dataService.deletePrivateMessage(msgId, convId, forEveryone, currentUser.id);
    setPrivateMessages((prev) =>
      forEveryone ? prev.filter((m) => m.id !== msgId) : prev.filter((m) => m.id !== msgId)
    );
    showToast(forEveryone ? 'تم حذف الرسالة لدى الجميع' : 'تم حذف الرسالة لديك فقط', 'INFO');
    return true;
  };

  const startCall = async (targetUser: User) => {
    if (!currentUser) return;
    const session: CallSession = {
      id: 'call_' + Math.random().toString(36).substring(2, 9),
      callerId: currentUser.id,
      callerName: currentUser.name,
      callerUsername: currentUser.username,
      callerAvatar: currentUser.avatarUrl,
      receiverId: targetUser.id,
      receiverName: targetUser.name,
      receiverUsername: targetUser.username,
      receiverAvatar: targetUser.avatarUrl,
      channelId: `ch_${Date.now()}`,
      status: 'ringing',
      timestamp: Date.now(),
    };

    await dataService.createCallSession(session);
    setActiveCallSession(session);
    setCallUiState('OUTGOING_RINGING');
    ringtoneManager.startOutgoingRingtone();

    setTimeout(() => {
      ringtoneManager.stop();
      setCallUiState('CONNECTED');
      voiceEngineRef.current.connectVoice(true);
      showToast('تم الاتصال بالطرف الآخر بنجاح!', 'SUCCESS');
    }, 4000);
  };

  const answerCall = async () => {
    ringtoneManager.stop();
    if (activeCallSession) {
      await dataService.updateCallStatus(activeCallSession.id, 'connected');
    }
    setCallUiState('CONNECTED');
    await voiceEngineRef.current.connectVoice(true);
    showToast('المكالمة متصلة الآن ومشفرة', 'SUCCESS');
  };

  const declineCall = async () => {
    ringtoneManager.stop();
    if (activeCallSession) {
      await dataService.updateCallStatus(activeCallSession.id, 'declined');
    }
    setCallUiState('IDLE');
    setActiveCallSession(null);
    showToast('تم رفض المكالمة', 'INFO');
  };

  const endCall = async () => {
    ringtoneManager.stop();
    voiceEngineRef.current.disconnectVoice();
    if (activeCallSession) {
      await dataService.updateCallStatus(activeCallSession.id, 'ended');
    }
    setCallUiState('IDLE');
    setActiveCallSession(null);
    showToast('تم إنهاء المكالمة', 'INFO');
  };

  const openInAppBrowser = (url = 'https://owner.cybenode.site/', title = 'معلومات المطور') => {
    if (currentScreen !== 'BROWSER') {
      setPreviousScreenBeforeBrowser(currentScreen);
    }
    setBrowserUrl(url);
    setBrowserTitle(title);
    setCurrentScreen('BROWSER');
  };

  const closeInAppBrowser = () => {
    setCurrentScreen(previousScreenBeforeBrowser);
  };

  const mySavedRooms = allRooms.filter((r: RoomInfo) => currentUser && r.hostId === currentUser.id);
  const friendsRooms = allRooms.filter((r: RoomInfo) => !currentUser || r.hostId !== currentUser.id);

  return (
    <AppContext.Provider
      value={{
        currentScreen,
        navigateTo,
        currentUser,
        register,
        login,
        logout,
        updateProfile,
        toasts,
        showToast,
        dismissToast,
        isVoiceConnected,
        isVoiceMuted,
        isVoiceDeafened,
        isVoiceSpeaking,
        soundWaveAmplitudes,
        toggleVoiceConnection,
        toggleVoiceMute,
        toggleVoiceDeafen,
        allRooms,
        mySavedRooms,
        friendsRooms,
        currentRoom,
        roomUsers,
        roomMessages,
        createRoom,
        joinRoom,
        leaveRoom,
        deleteCurrentRoom,
        sendRoomMessage,
        refreshRooms,
        forumMessages,
        sendForumMessage,
        refreshForum,
        friends,
        friendRequests,
        searchUserResult,
        isSearchingUser,
        searchUser,
        sendFriendRequest,
        acceptFriendRequest,
        declineFriendRequest,
        toggleBlockFriend,
        removeFriend,
        refreshFriends,
        selectedUserProfile,
        viewUserProfile,
        conversations,
        activeDirectUser,
        openDirectChat,
        privateMessages,
        sendPrivateTextMessage,
        sendPrivateVoiceMessage,
        deletePrivateMessage,
        refreshConversations,
        callUiState,
        activeCallSession,
        callDurationSec,
        startCall,
        answerCall,
        declineCall,
        endCall,
        browserUrl,
        browserTitle,
        openInAppBrowser,
        closeInAppBrowser,
        downloadUrl,
        supportUrl,
        privacyUrl,
      }}
    >
      {children}
    </AppContext.Provider>
  );
};

export const useApp = () => {
  const context = useContext(AppContext);
  if (!context) {
    throw new Error('useApp must be used within an AppProvider');
  }
  return context;
};
