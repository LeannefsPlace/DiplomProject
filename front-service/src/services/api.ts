import axios from 'axios';
import { UserModel } from '../types/UserModel';

const AUTH_API_URL = 'http://localhost:8090';

// Создаем экземпляр axios с базовым URL
const api = axios.create({
  baseURL: AUTH_API_URL,
});

// Добавляем перехватчик для добавления токена к запросам
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('auth_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export const authApi = {
  async login(userModel: UserModel): Promise<string> {
    const response = await api.post('/auth/login', userModel);
    return response.data;
  },

  async register(userModel: UserModel): Promise<string> {
    const response = await api.post('/auth/register', userModel);
    return response.data;
  }
}; 