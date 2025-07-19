// Common types used across API responses

export interface Role {
  id: string;
  name: string;
  description?: string;
}

export interface ExperienceLevel {
  id: string;
  name: string;
  description?: string;
}

export interface RoadmapItem {
  id: string;
  title: string;
  items: string[];
  completed?: boolean;
}

export interface RoadmapResponse {
  role: string;
  experience: string;
  timeline: string;
  roadmap: RoadmapItem[];
  resources?: Array<{
    type: 'course' | 'book' | 'tutorial' | 'documentation' | 'article' | 'video' | 'other';
    title: string;
    url: string;
    author?: string;
    provider?: string;
    difficulty?: 'beginner' | 'intermediate' | 'advanced';
  }>;
}

export interface InterviewQuestion {
  id: string;
  question: string;
  category: string;
  difficulty: 'easy' | 'medium' | 'hard';
  tags?: string[];
  answer?: string;
  example?: string;
}

export interface InterviewRound {
  id: string;
  name: string;
  description?: string;
  duration?: number; // in minutes
  questions: InterviewQuestion[];
}

export interface Skill {
  id: string;
  name: string;
  category: string;
  proficiency?: number; // 0-100
  isCore?: boolean;
}

// API Response Types
export interface ApiResponse<T> {
  data: T;
  status: number;
  message?: string;
}

// API Error Response
export interface ApiError {
  message: string;
  status: number;
  code?: string;
  details?: Record<string, any>;
}

// API Request Types
export interface PaginationParams {
  page?: number;
  limit?: number;
  sortBy?: string;
  sortOrder?: 'asc' | 'desc';
}

export interface SearchParams extends PaginationParams {
  query?: string;
  filters?: Record<string, any>;
}
