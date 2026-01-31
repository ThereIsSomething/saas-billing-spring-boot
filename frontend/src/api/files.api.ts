import api from './client';
import { UploadedFile } from '../types';

export const filesApi = {
    getMine: async (): Promise<UploadedFile[]> => {
        const response = await api.get<UploadedFile[]>('/files/my');
        return response.data;
    },

    upload: async (file: File): Promise<UploadedFile> => {
        const formData = new FormData();
        formData.append('file', file);

        const response = await api.post<UploadedFile>('/files', formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
            },
        });
        return response.data;
    },

    download: async (id: string): Promise<Blob> => {
        const response = await api.get(`/files/${id}/download`, {
            responseType: 'blob',
        });
        return response.data;
    },

    delete: async (id: string): Promise<void> => {
        await api.delete(`/files/${id}`);
    },

    getById: async (id: string): Promise<UploadedFile> => {
        const response = await api.get<UploadedFile>(`/files/${id}`);
        return response.data;
    },
};
