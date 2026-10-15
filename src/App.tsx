import React from 'react';
import { useApp } from './context/AppContext';
import { SplashScreen } from './screens/SplashScreen';
import { PermissionsScreen } from './screens/PermissionsScreen';
import { LandingScreen } from './screens/LandingScreen';
import { DashboardScreen } from './screens/DashboardScreen';
import { ActiveRoomScreen } from './screens/ActiveRoomScreen';
import { FriendsScreen } from './screens/FriendsScreen';
import { PrivateConversationsScreen } from './screens/PrivateConversationsScreen';
import { DirectChatScreen } from './screens/DirectChatScreen';
import { ForumScreen } from './screens/ForumScreen';
import { ProfileScreen } from './screens/ProfileScreen';
import { UserProfileViewScreen } from './screens/UserProfileViewScreen';
import { PrivacyPolicyScreen } from './screens/PrivacyPolicyScreen';
import { InAppBrowserScreen } from './screens/InAppBrowserScreen';
import { CallOverlay } from './components/CallOverlay';
import { ToastContainer } from './components/ToastContainer';

export const AppContent: React.FC = () => {
  const { currentScreen } = useApp();

  const renderScreen = () => {
    switch (currentScreen) {
      case 'SPLASH':
        return <SplashScreen />;
      case 'PERMISSIONS':
        return <PermissionsScreen />;
      case 'LANDING':
        return <LandingScreen />;
      case 'DASHBOARD':
        return <DashboardScreen />;
      case 'ACTIVE_ROOM':
        return <ActiveRoomScreen />;
      case 'FRIENDS':
        return <FriendsScreen />;
      case 'PRIVATE_CHATS':
        return <PrivateConversationsScreen />;
      case 'DIRECT_CHAT':
        return <DirectChatScreen />;
      case 'FORUM':
        return <ForumScreen />;
      case 'PROFILE':
        return <ProfileScreen />;
      case 'USER_PROFILE':
        return <UserProfileViewScreen />;
      case 'PRIVACY':
        return <PrivacyPolicyScreen />;
      case 'BROWSER':
        return <InAppBrowserScreen />;
      default:
        return <DashboardScreen />;
    }
  };

  return (
    <div className="min-h-screen bg-[#040711] text-[#F8FAFC]">
      {renderScreen()}
      <CallOverlay />
      <ToastContainer />
    </div>
  );
};
