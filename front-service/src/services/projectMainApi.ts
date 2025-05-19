import axios from 'axios';
import {ProjectDTO} from "../types/ProjectDTO.ts";

const AUTH_API_URL = 'http://localhost:8080';

const api = axios.create({
    baseURL: AUTH_API_URL,
});

api.interceptors.request.use((config) => {
    const token = localStorage.getItem('auth_token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

export const projectsApi = {
    async getAllProjects(): Promise<ProjectDTO[]> {
        const response = await api.get('/projects/list');
        return response.data;
    },

    async createNewProject(projectDTO:ProjectDTO): Promise<ProjectDTO> {
        const response = await api.post('/auth/register', projectDTO);
        return response.data;
    }
};