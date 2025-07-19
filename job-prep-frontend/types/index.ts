export interface Resource {
  name: string;
  url: string;
}

export interface Skill {
  id: string;
  name: string;
  description: string;
  level: string;
  completed: boolean;
  resources: Resource[];
}

export interface RoleRoadmap {
  id: string;
  role: string;
  experience: string;
  overview: string;
  skills: Skill[];
  resources?: Resource[];
}

export interface SkillRoadmap {
  id: string;
  name: string;
  description: string;
  level: string;
  topics: string[];
  resources: Resource[];
  interviewQuestions: InterviewQuestion[];
}

export interface InterviewQuestion {
  question: string;
  answer: string;
}

export interface ApiResponse<T> {
  data?: T;
  error?: string;
}
