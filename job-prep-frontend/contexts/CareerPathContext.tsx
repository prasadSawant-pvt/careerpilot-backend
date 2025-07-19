import React, { createContext, useContext, useCallback, useMemo, ReactNode } from 'react';
import { useApi } from '@/hooks/useApi';
import ApiService from '@/services/api';
import type { Role, ExperienceLevel, RoadmapResponse, InterviewQuestion, InterviewRound } from '@/types/api';

interface CareerPathContextType {
  // State
  roles: Role[];
  experiences: ExperienceLevel[];
  selectedRole: Role | null;
  selectedExperience: ExperienceLevel | null;
  roadmap: RoadmapResponse | null;
  interviewQuestions: InterviewQuestion[];
  interviewRounds: InterviewRound[];
  isLoading: boolean;
  error: Error | null;
  
  // Actions
  fetchRoles: () => Promise<void>;
  fetchExperiences: () => Promise<void>;
  selectRole: (role: Role) => void;
  selectExperience: (experience: ExperienceLevel) => void;
  fetchRoadmap: (role: string, experience: string) => Promise<void>;
  fetchInterviewQuestions: (role: string, experience: string) => Promise<void>;
  fetchInterviewRounds: (role: string, experience: string) => Promise<void>;
  reset: () => void;
}

const CareerPathContext = createContext<CareerPathContextType | undefined>(undefined);

export const CareerPathProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  // API hooks
  const {
    data: rolesData,
    error: rolesError,
    isLoading: isLoadingRoles,
    execute: fetchRolesApi,
  } = useApi<{ roles: Role[] }>();

  const {
    data: experiencesData,
    error: experiencesError,
    isLoading: isLoadingExperiences,
    execute: fetchExperiencesApi,
  } = useApi<{ experiences: ExperienceLevel[] }>();

  const {
    data: roadmapData,
    error: roadmapError,
    isLoading: isLoadingRoadmap,
    execute: fetchRoadmapApi,
  } = useApi<RoadmapResponse>();

  const {
    data: questionsData,
    error: questionsError,
    isLoading: isLoadingQuestions,
    execute: fetchQuestionsApi,
  } = useApi<{ questions: InterviewQuestion[] }>();

  const {
    data: roundsData,
    error: roundsError,
    isLoading: isLoadingRounds,
    execute: fetchRoundsApi,
  } = useApi<{ rounds: InterviewRound[] }>();

  // Local state
  const [selectedRole, setSelectedRole] = React.useState<Role | null>(null);
  const [selectedExperience, setSelectedExperience] = React.useState<ExperienceLevel | null>(null);

  // Derived state
  const roles = rolesData?.roles || [];
  const experiences = experiencesData?.experiences || [];
  const roadmap = roadmapData || null;
  const interviewQuestions = questionsData?.questions || [];
  const interviewRounds = roundsData?.rounds || [];
  
  const isLoading = isLoadingRoles || isLoadingExperiences || isLoadingRoadmap || isLoadingQuestions || isLoadingRounds;
  const error = rolesError || experiencesError || roadmapError || questionsError || roundsError;

  // Memoized callbacks
  const fetchRoles = useCallback(async () => {
    await fetchRolesApi(() => ApiService.getRoles());
  }, [fetchRolesApi]);

  const fetchExperiences = useCallback(async () => {
    await fetchExperiencesApi(() => ApiService.getExperienceLevels());
  }, [fetchExperiencesApi]);

  const fetchRoadmap = useCallback(async (role: string, experience: string) => {
    await fetchRoadmapApi(() => ApiService.getRoadmap(role, experience));
  }, [fetchRoadmapApi]);

  const fetchInterviewQuestions = useCallback(async (role: string, experience: string) => {
    await fetchQuestionsApi(() => ApiService.getInterviewQuestions(role, experience));
  }, [fetchQuestionsApi]);

  const fetchInterviewRounds = useCallback(async (role: string, experience: string) => {
    await fetchRoundsApi(() => ApiService.getInterviewRounds(role, experience));
  }, [fetchRoundsApi]);

  const selectRole = useCallback((role: Role) => {
    setSelectedRole(role);
    // Reset dependent state when role changes
    setSelectedExperience(null);
  }, []);

  const selectExperience = useCallback((experience: ExperienceLevel) => {
    setSelectedExperience(experience);
  }, []);

  const reset = useCallback(() => {
    setSelectedRole(null);
    setSelectedExperience(null);
    // Reset all API states
    fetchRolesApi(() => ApiService.getRoles());
    fetchExperiencesApi(() => ApiService.getExperienceLevels());
  }, [fetchRolesApi, fetchExperiencesApi]);

  // Context value
  const value = useMemo(() => ({
    roles,
    experiences,
    selectedRole,
    selectedExperience,
    roadmap,
    interviewQuestions,
    interviewRounds,
    isLoading,
    error,
    fetchRoles,
    fetchExperiences,
    selectRole,
    selectExperience,
    fetchRoadmap,
    fetchInterviewQuestions,
    fetchInterviewRounds,
    reset,
  }), [
    roles,
    experiences,
    selectedRole,
    selectedExperience,
    roadmap,
    interviewQuestions,
    interviewRounds,
    isLoading,
    error,
    fetchRoles,
    fetchExperiences,
    selectRole,
    selectExperience,
    fetchRoadmap,
    fetchInterviewQuestions,
    fetchInterviewRounds,
    reset,
  ]);

  return (
    <CareerPathContext.Provider value={value}>
      {children}
    </CareerPathContext.Provider>
  );
};

export const useCareerPath = () => {
  const context = useContext(CareerPathContext);
  if (context === undefined) {
    throw new Error('useCareerPath must be used within a CareerPathProvider');
  }
  return context;
};

// Example usage in a component:
/*
const MyComponent = () => {
  const { 
    roles, 
    experiences, 
    selectedRole, 
    selectedExperience, 
    fetchRoles, 
    selectRole,
    selectExperience
  } = useCareerPath();

  useEffect(() => {
    fetchRoles();
  }, [fetchRoles]);

  // ... rest of the component
};
*/
