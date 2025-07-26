import {
  DetailedRoadmapResponse,
  RoadmapResponse,
  RoadmapItem,
  LearningResource,
  InterviewQuestion,
  InterviewQuestionResponse,
  InterviewRound,
  Skill,
  ExperienceLevel,
  Role,
} from '@/types/api';

/**
 * Transforms a detailed roadmap response to the frontend's RoadmapResponse format
 */
export const transformRoadmapResponse = (
  response: DetailedRoadmapResponse
): RoadmapResponse => {
  // Flatten the roadmap phases and topics into a single array of RoadmapItems
  const roadmapItems: RoadmapItem[] = [];
  const resources: LearningResource[] = [];

  response.phases?.forEach((phase) => {
    phase.topics?.forEach((topic) => {
      const item: RoadmapItem = {
        id: `${phase.phaseName}-${topic.topicName}`,
        title: topic.topicName,
        description: topic.description,
        items: topic.subtopics?.map((subtopic) => subtopic.name) || [],
        estimatedHours: topic.estimatedHours,
        difficulty: topic.difficulty as 'beginner' | 'intermediate' | 'advanced',
        phase: phase.phaseName,
        weekNumber: phase.weekNumber,
      };
      
      // Add topic resources
      topic.subtopics?.forEach((subtopic) => {
        subtopic.resources?.forEach((resource) => {
          resources.push({
            ...resource,
            type: (resource.type || 'other') as LearningResource['type'],
            topic: topic.topicName,
            subtopic: subtopic.name,
          });
        });
      });
      
      roadmapItems.push(item);
    });
  });

  return {
    id: response.id,
    role: response.role,
    experience: response.experienceLevel,
    compositeKey: response.compositeKey,
    timeline: response.estimatedWeeks ? `${response.estimatedWeeks} weeks` : 'Flexible',
    roadmap: roadmapItems,
    resources,
    requiredSkills: response.requiredSkills || [],
    prerequisites: response.prerequisites || [],
    metadata: response.metadata,
  };
};

/**
 * Transforms interview questions response to the frontend format
 */
export const transformInterviewQuestions = (
  response: InterviewQuestionResponse
): InterviewQuestion[] => {
  return response.questions?.map((question) => ({
    ...question,
    difficulty: (question.difficulty || 'medium') as 'easy' | 'medium' | 'hard',
    category: question.category || 'General',
  })) || [];
};

/**
 * Transforms interview rounds response to the frontend format
 */
export const transformInterviewRounds = (
  rounds: InterviewRound[]
): InterviewRound[] => {
  return rounds.map((round) => ({
    ...round,
    questions: round.questions?.map((q) => ({
      ...q,
      difficulty: (q.difficulty || 'medium') as 'easy' | 'medium' | 'hard',
      category: q.category || 'General',
    })) || [],
  }));
};

/**
 * Transforms skills response to the frontend format
 */
export const transformSkills = (skills: Skill[]): Skill[] => {
  return skills.map((skill) => ({
    ...skill,
    proficiency: skill.proficiency || 0,
    isCore: skill.isCore || false,
  }));
};

/**
 * Transforms roles response to the frontend format
 */
export const transformRoles = (roles: Role[]): Role[] => {
  return roles.map((role) => ({
    ...role,
    description: role.description || `Career path for ${role.name}`,
  }));
};

/**
 * Transforms experience levels response to the frontend format
 */
export const transformExperienceLevels = (
  experiences: ExperienceLevel[]
): ExperienceLevel[] => {
  return experiences.map((exp) => ({
    ...exp,
    description: exp.description || `Experience level: ${exp.name}`,
  }));
};

export default {
  transformRoadmapResponse,
  transformInterviewQuestions,
  transformInterviewRounds,
  transformSkills,
  transformRoles,
  transformExperienceLevels,
};
