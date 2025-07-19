import React, { createContext, useContext, useEffect, useState } from 'react';
import { useRouter } from 'next/router';

type User = {
  id: string;
  email: string;
  name: string;
  image?: string;
} | null;

type AuthContextType = {
  user: User;
  loading: boolean;
  signIn: (email: string, password: string) => Promise<void>;
  signUp: (email: string, password: string, name: string) => Promise<void>;
  signOut: () => Promise<void>;
  updateUser: (userData: Partial<User>) => void;
};

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User>(null);
  const [loading, setLoading] = useState(true);
  const router = useRouter();

  // Check if user is logged in on initial load
  useEffect(() => {
    // In a real app, you would check for an auth token in cookies/localStorage
    // and validate it with your backend
    const checkAuth = async () => {
      try {
        // Simulate API call to check auth status
        await new Promise(resolve => setTimeout(resolve, 500));
        
        // For demo purposes, we'll just set a mock user
        // In a real app, you would get this from your auth provider
        setUser({
          id: '1',
          email: 'demo@example.com',
          name: 'Demo User',
          image: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Demo',
        });
      } catch (error) {
        console.error('Auth check failed:', error);
        setUser(null);
      } finally {
        setLoading(false);
      }
    };

    checkAuth();
  }, []);

  const signIn = async (email: string, password: string) => {
    try {
      setLoading(true);
      // In a real app, you would make an API call to your authentication service
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      // For demo purposes, we'll just set a mock user
      setUser({
        id: '1',
        email,
        name: 'Demo User',
        image: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Demo',
      });
      
      // Redirect to dashboard or intended URL
      const redirectTo = router.query.redirectTo || '/';
      await router.push(redirectTo as string);
    } catch (error) {
      console.error('Sign in failed:', error);
      throw error;
    } finally {
      setLoading(false);
    }
  };

  const signUp = async (email: string, password: string, name: string) => {
    try {
      setLoading(true);
      // In a real app, you would make an API call to your authentication service
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      // For demo purposes, we'll just set a mock user
      setUser({
        id: '1',
        email,
        name,
        image: 'https://api.dicebear.com/7.x/avataaars/svg?seed=' + name,
      });
      
      // Redirect to dashboard or intended URL
      const redirectTo = router.query.redirectTo || '/';
      await router.push(redirectTo as string);
    } catch (error) {
      console.error('Sign up failed:', error);
      throw error;
    } finally {
      setLoading(false);
    }
  };

  const signOut = async () => {
    try {
      setLoading(true);
      // In a real app, you would make an API call to sign out
      await new Promise(resolve => setTimeout(resolve, 300));
      
      setUser(null);
      await router.push('/auth/signin');
    } catch (error) {
      console.error('Sign out failed:', error);
      throw error;
    } finally {
      setLoading(false);
    }
  };

  const updateUser = (userData: Partial<User>) => {
    if (user) {
      setUser({ ...user, ...userData });
    }
  };

  const value = {
    user,
    loading,
    signIn,
    signUp,
    signOut,
    updateUser,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
