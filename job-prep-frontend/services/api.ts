import type {
  Role,
  ExperienceLevel,
  RoadmapResponse,
  DetailedRoadmapResponse,
  InterviewQuestion,
  InterviewQuestionResponse,
  InterviewRound,
  Skill,
  GenerateQuestionsRequest,
  DetailedRoadmapRequest,
  PaginationParams,
  SearchParams,
  ApiResponse,
  ApiError
} from '@/types/api';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080/api';
const DEFAULT_HEADERS = {
  'Content-Type': 'application/json',
  'Accept': 'application/json',
};

// Helper function to handle API responses
const handleResponse = async <T>(response: Response): Promise<ApiResponse<T>> => {
  const data = await response.json().catch(() => ({}));
  
  if (!response.ok) {
    const error: ApiError = {
      message: data.message || 'Something went wrong',
      status: response.status,
      code: data.code,
      details: data.details
    };
    throw error;
  }
  
  return {
    ...data,
    statusCode: response.status,
  };
};

// API Service Class
class ApiService {
  // Helper method for GET requests
  private static async get<T>(endpoint: string, params?: Record<string, any>): Promise<ApiResponse<T>> {
    const url = new URL(`${API_BASE_URL}${endpoint}`);
    
    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null) {
          url.searchParams.append(key, String(value));
        }
      });
    }
    
    const response = await fetch(url.toString(), {
      method: 'GET',
      headers: DEFAULT_HEADERS,
      credentials: 'include',
    });
    
    return handleResponse<T>(response);
  }
  
  // Helper method for POST requests
  private static async post<T>(
    endpoint: string, 
    body: any,
    params?: Record<string, any>
  ): Promise<ApiResponse<T>> {
    const url = new URL(`${API_BASE_URL}${endpoint}`);
    
    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null) {
          url.searchParams.append(key, String(value));
        }
      });
    }
    
    const response = await fetch(url.toString(), {
      method: 'POST',
      headers: DEFAULT_HEADERS,
      credentials: 'include',
      body: JSON.stringify(body),
    });
    
    return handleResponse<T>(response);
  }

  // Roles API with enhanced error handling
  static async getRoles(): Promise<ApiResponse<Role[]>> {
    try {
      return await this.get<Role[]>('/roles');
    } catch (error) {
      console.warn('Failed to fetch roles from /roles endpoint, trying /api/roles');
      // Try with /api prefix if the first attempt fails
      try {
        return await this.get<Role[]>('/api/roles');
      } catch (innerError) {
        console.warn('Failed to fetch roles from /api/roles endpoint');
        // Return a successful response with empty data to trigger fallback in the UI
        return {
          success: true,
          message: 'Using fallback roles',
          data: [],
          statusCode: 200
        };
      }
    }
  }

  // Experiences API
  static async getExperienceLevels(): Promise<ApiResponse<ExperienceLevel[]>> {
    return this.get<ExperienceLevel[]>('/experiences');
  }

  // Roadmap API - Get detailed roadmap (GET)
  static async getDetailedRoadmap(
    role: string, 
    experienceLevel: string,
    currentSkills?: string[],
    timelineWeeks?: number,
    focusArea?: string,
    forceRegenerate: boolean = false
  ): Promise<ApiResponse<DetailedRoadmapResponse>> {
    // Build the endpoint URL with query parameters
    const endpoint = '/roadmaps/detailed';
    
    // Include required and optional parameters
    const params: Record<string, any> = {
      role: role,
      experienceLevel: experienceLevel,
      forceRegenerate: forceRegenerate
    };
    
    // Add optional parameters if provided
    if (currentSkills?.length) params.currentSkills = currentSkills.join(',');
    if (timelineWeeks) params.timelineWeeks = timelineWeeks;
    if (focusArea) params.focusArea = focusArea;
    
    return this.get<DetailedRoadmapResponse>(endpoint, params);
  }

  // Regenerate detailed roadmap (POST)
  static async regenerateDetailedRoadmap(
    role: string, 
    experienceLevel: string,
    currentSkills?: string[],
    timelineWeeks?: number,
    focusArea?: string,
    forceRegenerate: boolean = true
  ): Promise<ApiResponse<DetailedRoadmapResponse>> {
    const endpoint = '/roadmaps/detailed';
    
    // Create request body
    const requestBody: DetailedRoadmapRequest = {
      role,
      experienceLevel,
      currentSkills,
      timelineWeeks,
      focusArea,
      forceRegenerate
    };
    
    return this.post<DetailedRoadmapResponse>(endpoint, requestBody);
  }

  // Generate Interview Questions
  static async generateInterviewQuestions(
    request: GenerateQuestionsRequest
  ): Promise<ApiResponse<InterviewQuestionResponse>> {
    return this.post<InterviewQuestionResponse>('/interview-questions', request);
  }

  // Get Interview Questions
  static async getInterviewQuestions(
    role: string,
    experience: string,
    count: number = 10,
    topics?: string,
    forceRefresh: boolean = false
  ): Promise<ApiResponse<InterviewQuestionResponse>> {
    const request: GenerateQuestionsRequest = {
      role,
      experienceLevel: experience,
      count,
      topics,
      forceRefresh,
    };
    
    return this.generateInterviewQuestions(request);
  }

  // Get Interview Rounds
  static async getInterviewRounds(
    role: string,
    experience: string
  ): Promise<ApiResponse<InterviewRound[]>> {
    return this.get<InterviewRound[]>('/interview-rounds', { role, experienceLevel: experience });
  }

  // Search Skills
  static async searchSkills(
    query: string,
    pagination?: PaginationParams
  ): Promise<ApiResponse<Skill[]>> {
    const params: SearchParams = { query, ...pagination };
    return this.get<Skill[]>('/skills', params);
  }
  
  // AI Query
  static async queryAI(prompt: string, model?: string): Promise<ApiResponse<{ result: string }>> {
    return this.post<{ result: string }>('/ai/query', { prompt, model });
  }
}

export default ApiService;
