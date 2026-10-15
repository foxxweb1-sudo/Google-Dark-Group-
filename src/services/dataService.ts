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
} from '../types';

const RTDB_BASE = 'https://group-afc70-default-rtdb.firebaseio.com';
const STORAGE_PREFIX = 'darkgroup_';

const SEED_USERS: User[] = [
  {
    id: 'usr_owner_01',
    username: 'foxx_root',
    name: 'المطور الرئيسي / Root',
    avatarUrl: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150&auto=format&fit=crop&q=80',
    isAnon: false,
    createdAt: Date.now() - 86400000 * 30,
  },
  {
    id: 'usr_ghost_02',
    username: 'cipher_sec',
    name: 'شبح التشفير ⚡',
    avatarUrl: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&auto=format&fit=crop&q=80',
    isAnon: true,
    createdAt: Date.now() - 86400000 * 14,
  },
  {
    id: 'usr_neo_03',
    username: 'matrix_node',
    name: 'مهندس النظم (Matrix)',
    avatarUrl: 'https://images.unsplash.com/photo-1517841905240-472988babdf9?w=150&auto=format&fit=crop&q=80',
    isAnon: false,
    createdAt: Date.now() - 86400000 * 7,
  },
];

const SEED_ROOMS: RoomInfo[] = [
  {
    id: 'ROOM-8821',
    name: 'غرفة العمليات المركزية [Node #01]',
    host: 'المطور الرئيسي / Root',
    hostId: 'usr_owner_01',
    password: null,
    isLocked: false,
    createdAt: Date.now() - 3600000 * 4,
  },
  {
    id: 'CYBER-773',
    name: 'نقاشات الأمن السيبراني والتشفير',
    host: 'شبح التشفير ⚡',
    hostId: 'usr_ghost_02',
    password: '123',
    isLocked: true,
    createdAt: Date.now() - 3600000 * 2,
  },
  {
    id: 'AUDIO-550',
    name: 'صوتيات مباشرة مفتوحة للجميع 🎙️',
    host: 'مهندس النظم (Matrix)',
    hostId: 'usr_neo_03',
    password: null,
    isLocked: false,
    createdAt: Date.now() - 1800000,
  },
];

const SEED_FORUM: ChatMessage[] = [
  {
    id: 'fmsg_1',
    roomCode: 'FORUM',
    senderId: 'usr_owner_01',
    senderName: 'المطور الرئيسي / Root',
    text: 'مرحباً بجميع رواد شبكة Dark Group! المنظومة تعتمد بروتوكول اتصال فوري مشفر وغرف صوتية مباشرة عالية النقاء.',
    time: '10:30 ص',
    timestamp: Date.now() - 3600000 * 5,
    avatarUrl: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150&auto=format&fit=crop&q=80',
    isSystem: false,
  },
  {
    id: 'fmsg_2',
    roomCode: 'FORUM',
    senderId: 'usr_ghost_02',
    senderName: 'شبح التشفير ⚡',
    text: 'تم فحص استقرار خوادم الصوت والاتصال الداخلي P2P، الأداء ممتاز وزمن الاستجابة منخفض جداً.',
    time: '11:15 ص',
    timestamp: Date.now() - 3600000 * 3,
    avatarUrl: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&auto=format&fit=crop&q=80',
    isSystem: false,
  },
];

class DataService {
  private getStore<T>(key: string, fallback: T): T {
    try {
      const data = localStorage.getItem(STORAGE_PREFIX + key);
      return data ? JSON.parse(data) : fallback;
    } catch {
      return fallback;
    }
  }

  private setStore<T>(key: string, val: T): void {
    try {
      localStorage.setItem(STORAGE_PREFIX + key, JSON.stringify(val));
    } catch (e) {
      console.warn('Storage write failed', e);
    }
  }

  constructor() {
    if (typeof window !== 'undefined') {
      if (!localStorage.getItem(STORAGE_PREFIX + 'users')) {
        this.setStore('users', SEED_USERS);
      }
      if (!localStorage.getItem(STORAGE_PREFIX + 'rooms')) {
        this.setStore('rooms', SEED_ROOMS);
      }
      if (!localStorage.getItem(STORAGE_PREFIX + 'forum_messages')) {
        this.setStore('forum_messages', SEED_FORUM);
      }
    }
  }

  getCurrentUser(): User | null {
    return this.getStore<User | null>('current_user', null);
  }

  setCurrentUser(user: User | null): void {
    this.setStore('current_user', user);
  }

  async getUser(username: string): Promise<User | null> {
    const clean = username.toLowerCase().trim();
    const users = this.getStore<User[]>('users', SEED_USERS);
    const localFound = users.find((u: User) => u.username.toLowerCase() === clean);
    if (localFound) return localFound;

    try {
      const res = await fetch(`${RTDB_BASE}/users/${clean}.json`);
      if (res.ok) {
        const data = await res.json();
        if (data && data.username) {
          const user: User = {
            id: data.id || `usr_${data.username}`,
            username: data.username,
            name: data.name || data.username,
            avatarUrl: data.avatarUrl || null,
            isAnon: !!data.isAnon,
            password: data.password || '',
            createdAt: data.createdAt || Date.now(),
          };
          this.saveUserLocally(user);
          return user;
        }
      }
    } catch {}
    return null;
  }

  async getUserById(userId: string): Promise<User | null> {
    const users = this.getStore<User[]>('users', SEED_USERS);
    const found = users.find((u: User) => u.id === userId);
    if (found) return found;
    return null;
  }

  private saveUserLocally(user: User) {
    const users = this.getStore<User[]>('users', SEED_USERS);
    const filtered = users.filter((u: User) => u.username.toLowerCase() !== user.username.toLowerCase() && u.id !== user.id);
    filtered.push(user);
    this.setStore('users', filtered);
  }

  async saveUser(user: User): Promise<boolean> {
    this.saveUserLocally(user);
    try {
      await fetch(`${RTDB_BASE}/users/${user.username.toLowerCase()}.json`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(user),
      });
    } catch {}
    return true;
  }

  async updateUser(username: string, name: string, password?: string, avatarUrl?: string | null): Promise<boolean> {
    const users = this.getStore<User[]>('users', SEED_USERS);
    const idx = users.findIndex((u: User) => u.username.toLowerCase() === username.toLowerCase());
    if (idx !== -1) {
      users[idx] = {
        ...users[idx],
        name,
        ...(password ? { password } : {}),
        ...(avatarUrl !== undefined ? { avatarUrl } : {}),
      };
      this.setStore('users', users);

      const currentUser = this.getCurrentUser();
      if (currentUser && currentUser.username.toLowerCase() === username.toLowerCase()) {
        this.setCurrentUser(users[idx]);
      }
    }
    return true;
  }

  async getRoom(roomCode: string): Promise<RoomInfo | null> {
    const rooms = this.getStore<RoomInfo[]>('rooms', SEED_ROOMS);
    const found = rooms.find((r: RoomInfo) => r.id.toLowerCase() === roomCode.toLowerCase());
    if (found) return found;
    return null;
  }

  async createRoom(room: RoomInfo): Promise<boolean> {
    const rooms = this.getStore<RoomInfo[]>('rooms', SEED_ROOMS);
    const filtered = rooms.filter((r: RoomInfo) => r.id !== room.id);
    filtered.unshift(room);
    this.setStore('rooms', filtered);
    return true;
  }

  async deleteRoom(roomCode: string): Promise<boolean> {
    const rooms = this.getStore<RoomInfo[]>('rooms', SEED_ROOMS);
    const filtered = rooms.filter((r: RoomInfo) => r.id.toLowerCase() !== roomCode.toLowerCase());
    this.setStore('rooms', filtered);
    return true;
  }

  async fetchAllRooms(): Promise<RoomInfo[]> {
    return this.getStore<RoomInfo[]>('rooms', SEED_ROOMS);
  }

  async joinRoomUser(roomCode: string, user: RoomUser): Promise<boolean> {
    const key = `room_users_${roomCode}`;
    const users = this.getStore<RoomUser[]>(key, []);
    const filtered = users.filter((u: RoomUser) => u.id !== user.id);
    filtered.push(user);
    this.setStore(key, filtered);
    return true;
  }

  async leaveRoomUser(roomCode: string, userId: string): Promise<boolean> {
    const key = `room_users_${roomCode}`;
    const users = this.getStore<RoomUser[]>(key, []);
    const filtered = users.filter((u: RoomUser) => u.id !== userId);
    this.setStore(key, filtered);
    return true;
  }

  async updateVoiceState(roomCode: string, userId: string, isVoice: boolean, isMuted: boolean, isSpeaking = false): Promise<boolean> {
    const key = `room_users_${roomCode}`;
    const users = this.getStore<RoomUser[]>(key, []);
    const idx = users.findIndex((u: RoomUser) => u.id === userId);
    if (idx !== -1) {
      users[idx] = {
        ...users[idx],
        isVoice,
        isMuted,
        isSpeaking,
      };
      this.setStore(key, users);
    }
    return true;
  }

  async fetchRoomUsers(roomCode: string): Promise<RoomUser[]> {
    const key = `room_users_${roomCode}`;
    const users = this.getStore<RoomUser[]>(key, []);
    if (users.length === 0) {
      return [
        {
          id: 'usr_owner_01',
          peerId: 'peer_owner',
          name: 'المطور الرئيسي / Root',
          username: 'foxx_root',
          avatarUrl: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150&auto=format&fit=crop&q=80',
          isVoice: true,
          isMuted: false,
          isSpeaking: true,
        },
        {
          id: 'usr_ghost_02',
          peerId: 'peer_cipher',
          name: 'شبح التشفير ⚡',
          username: 'cipher_sec',
          avatarUrl: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&auto=format&fit=crop&q=80',
          isVoice: true,
          isMuted: true,
          isSpeaking: false,
        },
      ];
    }
    return users;
  }

  async sendRoomMessage(roomCode: string, msg: ChatMessage): Promise<boolean> {
    const key = `room_msgs_${roomCode}`;
    const messages = this.getStore<ChatMessage[]>(key, []);
    messages.push(msg);
    this.setStore(key, messages);
    return true;
  }

  async fetchRoomMessages(roomCode: string): Promise<ChatMessage[]> {
    const key = `room_msgs_${roomCode}`;
    const messages = this.getStore<ChatMessage[]>(key, []);
    if (messages.length === 0) {
      return [
        {
          id: 'sys_start',
          roomCode,
          senderId: 'system',
          senderName: 'النظام المشفر',
          text: `تم بدء تشفير الجلسة للغرفة ${roomCode}. المحادثة محمية من الطرف إلى الطرف.`,
          time: new Date().toLocaleTimeString('ar-EG', { hour: '2-digit', minute: '2-digit' }),
          timestamp: Date.now() - 600000,
          isSystem: true,
        },
      ];
    }
    return messages;
  }

  async fetchForumMessages(): Promise<ChatMessage[]> {
    return this.getStore<ChatMessage[]>('forum_messages', SEED_FORUM);
  }

  async sendForumMessage(msg: ChatMessage): Promise<boolean> {
    const messages = this.getStore<ChatMessage[]>('forum_messages', SEED_FORUM);
    messages.push(msg);
    this.setStore('forum_messages', messages);
    return true;
  }

  async fetchFriends(userId: string): Promise<Friend[]> {
    const key = `friends_${userId}`;
    const friends = this.getStore<Friend[]>(key, []);
    if (friends.length === 0) {
      const initial: Friend[] = [
        {
          userId: 'usr_owner_01',
          username: 'foxx_root',
          name: 'المطور الرئيسي / Root',
          avatarUrl: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150&auto=format&fit=crop&q=80',
          isBlocked: false,
          isOnline: true,
          activeRoomCode: 'ROOM-8821',
        },
        {
          userId: 'usr_ghost_02',
          username: 'cipher_sec',
          name: 'شبح التشفير ⚡',
          avatarUrl: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&auto=format&fit=crop&q=80',
          isBlocked: false,
          isOnline: true,
          activeRoomCode: 'CYBER-773',
        },
      ];
      this.setStore(key, initial);
      return initial;
    }
    return friends;
  }

  async fetchFriendRequests(username: string): Promise<FriendRequest[]> {
    const key = `friend_requests_${username.toLowerCase()}`;
    return this.getStore<FriendRequest[]>(key, [
      {
        id: 'req_seed_1',
        fromUserId: 'usr_neo_03',
        fromUsername: 'matrix_node',
        fromName: 'مهندس النظم (Matrix)',
        fromAvatarUrl: 'https://images.unsplash.com/photo-1517841905240-472988babdf9?w=150&auto=format&fit=crop&q=80',
        toUsername: username.toLowerCase(),
        timestamp: Date.now() - 3600000,
      },
    ]);
  }

  async sendFriendRequest(req: FriendRequest): Promise<boolean> {
    const key = `friend_requests_${req.toUsername.toLowerCase()}`;
    const requests = this.getStore<FriendRequest[]>(key, []);
    const exists = requests.some((r: FriendRequest) => r.fromUserId === req.fromUserId);
    if (!exists) {
      requests.push(req);
      this.setStore(key, requests);
    }
    return true;
  }

  async acceptFriendRequest(req: FriendRequest, currentUserId: string): Promise<boolean> {
    const reqKey = `friend_requests_${req.toUsername.toLowerCase()}`;
    const requests = this.getStore<FriendRequest[]>(reqKey, []);
    this.setStore(
      reqKey,
      requests.filter((r: FriendRequest) => r.id !== req.id)
    );

    const myFriendsKey = `friends_${currentUserId}`;
    const myFriends = this.getStore<Friend[]>(myFriendsKey, []);
    if (!myFriends.some((f: Friend) => f.userId === req.fromUserId)) {
      myFriends.push({
        userId: req.fromUserId,
        username: req.fromUsername,
        name: req.fromName,
        avatarUrl: req.fromAvatarUrl,
        isBlocked: false,
        isOnline: true,
        activeRoomCode: null,
      });
      this.setStore(myFriendsKey, myFriends);
    }
    return true;
  }

  async declineFriendRequest(reqId: string, username: string): Promise<boolean> {
    const reqKey = `friend_requests_${username.toLowerCase()}`;
    const requests = this.getStore<FriendRequest[]>(reqKey, []);
    this.setStore(
      reqKey,
      requests.filter((r: FriendRequest) => r.id !== reqId)
    );
    return true;
  }

  async blockFriend(userId: string, targetUserId: string, isBlocked: boolean): Promise<boolean> {
    const key = `friends_${userId}`;
    const friends = this.getStore<Friend[]>(key, []);
    const idx = friends.findIndex((f: Friend) => f.userId === targetUserId);
    if (idx !== -1) {
      friends[idx].isBlocked = isBlocked;
      this.setStore(key, friends);
    }
    return true;
  }

  async removeFriend(userId: string, targetUserId: string): Promise<boolean> {
    const key = `friends_${userId}`;
    const friends = this.getStore<Friend[]>(key, []);
    this.setStore(
      key,
      friends.filter((f: Friend) => f.userId !== targetUserId)
    );
    return true;
  }

  async fetchConversations(userId: string): Promise<PrivateConversation[]> {
    const key = `conversations_${userId}`;
    const convs = this.getStore<PrivateConversation[]>(key, []);
    if (convs.length === 0) {
      const initial: PrivateConversation[] = [
        {
          id: `conv_${userId}_usr_owner_01`,
          otherUserId: 'usr_owner_01',
          otherUsername: 'foxx_root',
          otherName: 'المطور الرئيسي / Root',
          otherAvatarUrl: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150&auto=format&fit=crop&q=80',
          lastMessage: 'أهلاً بك في النسخة المطورة لشبكة Dark Group المشفرة!',
          lastMessageTime: 'أمس',
          lastTimestamp: Date.now() - 86400000,
          isBlocked: false,
        },
      ];
      this.setStore(key, initial);
      return initial;
    }
    return convs;
  }

  async fetchPrivateMessages(conversationId: string, userId: string): Promise<PrivateChatMessage[]> {
    const key = `pchat_msgs_${conversationId}`;
    const msgs = this.getStore<PrivateChatMessage[]>(key, []);
    if (msgs.length === 0) {
      return [
        {
          id: 'pmsg_seed_1',
          conversationId,
          senderId: 'usr_owner_01',
          senderName: 'المطور الرئيسي / Root',
          text: 'أهلاً بك في النسخة المطورة لشبكة Dark Group المشفرة!',
          isVoice: false,
          voiceDurationSec: 0,
          time: '12:00 م',
          timestamp: Date.now() - 86400000,
          isMe: false,
          deletedForEveryone: false,
          deletedForUserIds: [],
        },
      ];
    }
    return msgs.filter((m: PrivateChatMessage) => !m.deletedForEveryone && !(m.deletedForUserIds || []).includes(userId));
  }

  async sendPrivateMessage(msg: PrivateChatMessage, otherUser: User, currentUser: User): Promise<boolean> {
    const convId = msg.conversationId;
    const key = `pchat_msgs_${convId}`;
    const msgs = this.getStore<PrivateChatMessage[]>(key, []);
    msgs.push(msg);
    this.setStore(key, msgs);

    const updateConv = (uId: string, other: User) => {
      const convKey = `conversations_${uId}`;
      const convList = this.getStore<PrivateConversation[]>(convKey, []);
      const existingIdx = convList.findIndex((c: PrivateConversation) => c.id === convId || c.otherUserId === other.id);
      const newConv: PrivateConversation = {
        id: convId,
        otherUserId: other.id,
        otherUsername: other.username,
        otherName: other.name,
        otherAvatarUrl: other.avatarUrl,
        lastMessage: msg.isVoice ? '🎙️ رسالة صوتية مشفرة' : msg.text,
        lastMessageTime: msg.time,
        lastTimestamp: msg.timestamp,
        isBlocked: false,
      };

      if (existingIdx !== -1) {
        convList[existingIdx] = newConv;
      } else {
        convList.unshift(newConv);
      }
      this.setStore(convKey, convList);
    };

    updateConv(currentUser.id, otherUser);
    updateConv(otherUser.id, currentUser);
    return true;
  }

  async deletePrivateMessage(msgId: string, conversationId: string, forEveryone: boolean, userId: string): Promise<boolean> {
    const key = `pchat_msgs_${conversationId}`;
    const msgs = this.getStore<PrivateChatMessage[]>(key, []);
    const idx = msgs.findIndex((m: PrivateChatMessage) => m.id === msgId);
    if (idx !== -1) {
      if (forEveryone) {
        msgs[idx].deletedForEveryone = true;
      } else {
        const deletedIds = msgs[idx].deletedForUserIds || [];
        if (!deletedIds.includes(userId)) {
          deletedIds.push(userId);
          msgs[idx].deletedForUserIds = deletedIds;
        }
      }
      this.setStore(key, msgs);
    }
    return true;
  }

  async createCallSession(session: CallSession): Promise<boolean> {
    const key = `call_${session.id}`;
    this.setStore(key, session);
    this.setStore(`incoming_call_${session.receiverId}`, session);
    return true;
  }

  async updateCallStatus(callId: string, status: CallSession['status']): Promise<boolean> {
    const key = `call_${callId}`;
    const session = this.getStore<CallSession | null>(key, null);
    if (session) {
      session.status = status;
      this.setStore(key, session);
      if (status === 'ended' || status === 'declined') {
        localStorage.removeItem(STORAGE_PREFIX + `incoming_call_${session.receiverId}`);
      }
    }
    return true;
  }

  checkIncomingCall(userId: string): CallSession | null {
    const session = this.getStore<CallSession | null>(`incoming_call_${userId}`, null);
    if (session && session.status === 'ringing') {
      return session;
    }
    return null;
  }

  getDownloadUrl(): string {
    return 'https://owner.cybenode.site/';
  }

  getSupportUrl(): string {
    return 'https://owner.cybenode.site/';
  }

  getPrivacyPolicyUrl(): string {
    return 'https://owner.cybenode.site/privacy';
  }
}

export const dataService = new DataService();
