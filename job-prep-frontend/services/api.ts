import { mockApi } from '@/utils/mockApi';
import type { 
  Role, 
  ExperienceLevel, 
  RoadmapResponse, 
  InterviewQuestion, 
  InterviewRound, 
  Skill,
  ApiResponse
} from '@/types/api';

// In a real implementation, this would be replaced with actual API calls
const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || '/api';
const USE_MOCK = process.env.NODE_ENV === 'development';

// Helper function to handle API responses
const handleResponse = async <T>(response: Response): Promise<ApiResponse<T>> => {
  if (!response.ok) {
    const error = await response.json().catch(() => ({}));
    throw {
      message: error.message || 'Something went wrong',
      status: response.status,
      ...error
    };
  }
  return response.json();
};

// API Service Class
class ApiService {
  // Roles API
  static async getRoles(): Promise<ApiResponse<{ roles: Role[] }>> {
    if (USE_MOCK) {
      return mockApi<{ roles: Role[] }>('/api/roles');
    }
    
    const response = await fetch(`${API_BASE_URL}/roles`);
    return handleResponse<{ roles: Role[] }>(response);
  }

  // Experiences API
  static async getExperienceLevels(): Promise<ApiResponse<{ experiences: ExperienceLevel[] }>> {
    if (USE_MOCK) {
      return mockApi<{ experiences: ExperienceLevel[] }>('/api/experiences');
    }
    
    const response = await fetch(`${API_BASE_URL}/experiences`);
    return handleResponse<{ experiences: ExperienceLevel[] }>(response);
  }

  // Roadmap API
  static async getRoadmap(role: string, experience: string): Promise<ApiResponse<RoadmapResponse>> {
    if (USE_MOCK) {
      return mockApi<RoadmapResponse>(`/api/roadmap/${role}/${experience}`);
    }
    
    const response = await fetch(`${API_BASE_URL}/roadmap/${role}/${experience}`);
    return handleResponse<RoadmapResponse>(response);
  }

  // Interview Questions API
  static async getInterviewQuestions(
    role: string, 
    experience: string
  ): Promise<ApiResponse<{ questions: InterviewQuestion[] }>> {
    if (USE_MOCK) {
      return mockApi<{ questions: InterviewQuestion[] }>(
        `/api/questions?role=${role}&experience=${experience}`
      );
    }
    
    const response = await fetch(
      `${API_BASE_URL}/questions?role=${role}&experience=${experience}`
    );
    return handleResponse<{ questions: InterviewQuestion[] }>(response);
  }

  // Interview Rounds API
  static async getInterviewRounds(
    role: string, 
    experience: string
  ): Promise<ApiResponse<{ rounds: InterviewRound[] }>> {
    if (USE_MOCK) {
      return mockApi<{ rounds: InterviewRound[] }>(
        `/api/interview-rounds?role=${role}&experience=${experience}`
      );
    }
    
    const response = await fetch(
      `${API_BASE_URL}/interview-rounds?role=${role}&experience=${experience}`
    );
    return handleResponse<{ rounds: InterviewRound[] }>(response);
  }

  // Search Skills API
  static async searchSkills(query: string): Promise<ApiResponse<{ skills: Skill[] }>> {
    if (USE_MOCK) {
      // Mock implementation would filter skills based on query
      return mockApi<{ skills: Skill[] }>('/api/skills', 'GET', { query: { q: query } });
    }
    
    const response = await fetch(`${API_BASE_URL}/skills?q=${encodeURIComponent(query)}`);
    return handleResponse<{ skills: Skill[] }>(response);
  }

  // Submit Query API
  static async submitQuery(query: string): Promise<ApiResponse<{ result: string }>> {
    if (USE_MOCK) {
      return mockApi<{ result: string }>(
        '/api/query', 
        'POST', 
        { body: { query } }
      );
    }
    
    const response = await fetch(`${API_BASE_URL}/query`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ query }),
    });
    
    return handleResponse<{ result: string }>(response);
  }
}

export default ApiService;
