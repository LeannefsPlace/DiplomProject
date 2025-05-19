import React, { createContext, useContext, useState, ReactNode, useEffect } from 'react';

interface AuthContextType {
  token: string | null;
  setToken: (token: string | null) => void;
  isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

// Функция для проверки валидности токена
const isTokenValid = (token: string): boolean => {
  try {
    // Здесь можно добавить проверку срока действия токена
    // Например, если токен содержит JWT, можно декодировать его и проверить exp
    return !!token;
  } catch {
    return false;
  }
};

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => {
    const savedToken = localStorage.getItem('auth_token');
    return savedToken && isTokenValid(savedToken) ? savedToken : null;
  });

  // Проверяем валидность токена при загрузке
  useEffect(() => {
    if (token && !isTokenValid(token)) {
      handleSetToken(null);
    }
  }, [token]);

  const handleSetToken = (newToken: string | null) => {
    if (newToken && isTokenValid(newToken)) {
      localStorage.setItem('auth_token', newToken);
    } else {
      localStorage.removeItem('auth_token');
    }
    setToken(newToken);
  };

  return (
    <AuthContext.Provider
      value={{
        token,
        setToken: handleSetToken,
        isAuthenticated: !!token && isTokenValid(token),
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}