// Common types used across API responses

// Base API Response Type
export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  statusCode: number;
}

// Role related types
export interface Role {
  id: string;
  name: string;
  description?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface ExperienceLevel {
  id: string;
  name: string;
  description?: string;
}

// Roadmap related types
export interface RoadmapItem {
  id: string;
  title: string;
  items: string[];
  completed?: boolean;
}

export interface LearningResource {
  title: string;
  description?: string;
  url: string;
  type?: 'course' | 'book' | 'tutorial' | 'documentation' | 'article' | 'video' | 'other';
  provider?: string;
  isFree?: boolean;
  estimatedDuration?: string;
  difficulty?: 'beginner' | 'intermediate' | 'advanced' | string;
  tags?: string[];
  rating?: number;
  author?: string;
  publicationYear?: number;
  language?: string;
}

export interface Subtopic {
  name: string;
  description?: string;
  keyPoints?: string[];
  estimatedHours?: number;
  practiceType?: string;
  resources?: LearningResource[];
  learningObjectives?: string[];
  difficulty?: string;
  completed?: boolean;
  notes?: string;
}

export interface Topic {
  topicName: string;
  description?: string;
  subtopics?: Subtopic[];
  estimatedHours?: number;
  difficulty?: string;
  associatedSkills?: string[];
  category?: string;
  keyConcepts?: string[];
  learningOutcomes?: string[];
  totalEstimatedHours?: number;
}

export interface RoadmapPhase {
  phaseName: string;
  weekNumber: number;
  objective?: string;
  topics: Topic[];
  deliverables?: string[];
}

export interface DetailedRoadmapResponse {
  id: string;
  role: string;
  experienceLevel: string;
  compositeKey: string;
  estimatedWeeks: number;
  phases: RoadmapPhase[];
  requiredSkills: string[];
  prerequisites: string[];
  metadata?: Record<string, any>;
  createdAt: string;
  updatedAt: string;
}

export interface RoadmapResponse {
  role: string;
  experience: string;
  timeline: string;
  roadmap: RoadmapItem[];
  resources?: LearningResource[];
}

// Interview related types
export interface InterviewQuestion {
  id: string;
  question: string;
  answer?: string;
  category?: string;
  difficulty?: 'easy' | 'medium' | 'hard' | string;
  tags?: string[];
  example?: string;
  role?: string;
  experience?: string;
  skill?: string;
}

export interface InterviewQuestionResponse {
  role: string;
  experienceLevel: string;
  questions: InterviewQuestion[];
}

export interface InterviewRound {
  id: string;
  name: string;
  description?: string;
  duration?: number; // in minutes
  questions: InterviewQuestion[];
}

// Skill related types
export interface Skill {
  id: string;
  name: string;
  category: string;
  proficiency?: number; // 0-100
  isCore?: boolean;
}

// Request types
export interface GenerateQuestionsRequest {
  role: string;
  experienceLevel: string;
  count: number;
  topics?: string;
  forceRefresh?: boolean;
}

export interface DetailedRoadmapRequest {
  role: string;
  experienceLevel: string;
  currentSkills?: string[];
  timelineWeeks?: number;
  focusArea?: string;
  compositeKey?: string;
  forceRegenerate?: boolean;
}

// API Error Response
export interface ApiError {
  message: string;
  status: number;
  code?: string;
  details?: Record<string, any>;
}

// Pagination and Search
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
