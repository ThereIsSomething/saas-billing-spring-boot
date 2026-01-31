import React, { useEffect, useState, useRef } from 'react';
import { filesApi } from '../../api';
import { UploadedFile } from '../../types';
import { Upload, File, Trash2, Download, FileText, Image, FileCode } from 'lucide-react';
import './FilesPage.css';

const FilesPage: React.FC = () => {
    const [files, setFiles] = useState<UploadedFile[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [isUploading, setIsUploading] = useState(false);
    const [deletingId, setDeletingId] = useState<string | null>(null);
    const fileInputRef = useRef<HTMLInputElement>(null);

    useEffect(() => {
        fetchFiles();
    }, []);

    const fetchFiles = async () => {
        try {
            const data = await filesApi.getMine();
            setFiles(data);
        } catch (error) {
            console.error('Error fetching files:', error);
        } finally {
            setIsLoading(false);
        }
    };

    const handleUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (!file) return;

        setIsUploading(true);
        try {
            await filesApi.upload(file);
            await fetchFiles();
        } catch (error) {
            console.error('Error uploading file:', error);
            alert('Failed to upload file');
        } finally {
            setIsUploading(false);
            if (fileInputRef.current) {
                fileInputRef.current.value = '';
            }
        }
    };

    const handleDelete = async (id: string) => {
        if (!confirm('Are you sure you want to delete this file?')) return;

        setDeletingId(id);
        try {
            await filesApi.delete(id);
            setFiles(files.filter(f => f.id !== id));
        } catch (error) {
            console.error('Error deleting file:', error);
        } finally {
            setDeletingId(null);
        }
    };

    const handleDownload = async (file: UploadedFile) => {
        try {
            const blob = await filesApi.download(file.id);
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = file.originalFileName;
            a.click();
            window.URL.revokeObjectURL(url);
        } catch (error) {
            console.error('Error downloading file:', error);
        }
    };

    const getFileIcon = (contentType: string) => {
        if (contentType.startsWith('image/')) return <Image size={24} />;
        if (contentType.includes('pdf') || contentType.includes('document')) return <FileText size={24} />;
        if (contentType.includes('code') || contentType.includes('javascript') || contentType.includes('json')) {
            return <FileCode size={24} />;
        }
        return <File size={24} />;
    };

    const formatFileSize = (bytes: number) => {
        if (bytes < 1024) return `${bytes} B`;
        if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
        return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
    };

    if (isLoading) {
        return (
            <div className="loading-overlay" style={{ position: 'relative', minHeight: '400px' }}>
                <div className="spinner" />
            </div>
        );
    }

    return (
        <div className="files-page">
            <div className="page-header">
                <div>
                    <h2>Files</h2>
                    <p className="text-secondary">Upload and manage your files</p>
                </div>
                <div className="upload-wrapper">
                    <input
                        type="file"
                        ref={fileInputRef}
                        onChange={handleUpload}
                        className="file-input-hidden"
                    />
                    <button
                        className="btn btn-primary"
                        onClick={() => fileInputRef.current?.click()}
                        disabled={isUploading}
                    >
                        {isUploading ? (
                            <>
                                <div className="spinner" style={{ width: 16, height: 16 }} />
                                Uploading...
                            </>
                        ) : (
                            <>
                                <Upload size={18} />
                                Upload File
                            </>
                        )}
                    </button>
                </div>
            </div>

            {files.length > 0 ? (
                <div className="files-grid">
                    {files.map((file) => (
                        <div key={file.id} className="file-card card">
                            <div className="file-icon">
                                {getFileIcon(file.contentType)}
                            </div>
                            <div className="file-info">
                                <span className="file-name" title={file.originalFileName}>
                                    {file.originalFileName}
                                </span>
                                <span className="file-meta">
                                    {formatFileSize(file.fileSize)} • {new Date(file.createdAt).toLocaleDateString()}
                                </span>
                            </div>
                            <div className="file-actions">
                                <button
                                    className="btn btn-ghost btn-icon"
                                    onClick={() => handleDownload(file)}
                                    title="Download"
                                >
                                    <Download size={18} />
                                </button>
                                <button
                                    className="btn btn-ghost btn-icon"
                                    onClick={() => handleDelete(file.id)}
                                    disabled={deletingId === file.id}
                                    title="Delete"
                                >
                                    {deletingId === file.id ? (
                                        <div className="spinner" style={{ width: 16, height: 16 }} />
                                    ) : (
                                        <Trash2 size={18} />
                                    )}
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            ) : (
                <div className="empty-state card">
                    <Upload size={48} className="empty-icon" />
                    <h3>No Files</h3>
                    <p>Upload your first file to get started.</p>
                    <button
                        className="btn btn-primary"
                        onClick={() => fileInputRef.current?.click()}
                    >
                        <Upload size={18} />
                        Upload File
                    </button>
                </div>
            )}
        </div>
    );
};

export default FilesPage;
