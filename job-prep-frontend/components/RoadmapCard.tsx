import React, { useState } from 'react';
import { DetailedRoadmapResponse, LearningResource } from '../types/api';
import Link from 'next/link';
import { motion, AnimatePresence } from 'framer-motion';
import { BookOpenIcon, CodeBracketIcon, DocumentTextIcon, VideoCameraIcon, AcademicCapIcon } from '@heroicons/react/24/outline';

interface RoadmapCardProps {
  roadmap: DetailedRoadmapResponse;
}

interface Skill {
  name: string;
  description: string;
  phaseIndex: number;
  topicIndex: number;
  subtopicIndex?: number;
  resources: LearningResource[];
  learningObjectives: string[];
}

interface Subtopic {
  name?: string;
  title?: string;
  description?: string;
  desc?: string;
  learningObjectives?: string[];
  keyPoints?: string[];
  resources?: LearningResource[];
  estimatedHours?: number;
}

interface Topic {
  id?: string;
  topicName?: string;
  name?: string;
  description?: string;
  desc?: string;
  subtopics?: Subtopic[];
  items?: Subtopic[];
  resources?: LearningResource[];
  learningObjectives?: string[];
  keyConcepts?: string[];
  estimatedHours?: number;
}

interface Phase {
  id?: string;
  phaseName?: string;
  name?: string;
  objective?: string;
  estimatedHours?: number;
  topics?: Topic[];
}

const formatExperienceLevel = (level: string) => {
  if (!level) return '';
  // Convert from "intermediate-(2-5-years)" to "Intermediate (2-5 years)"
  return level
    .split('-')
    .map(word => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ')
    .replace(/\(/g, ' (')
    .replace(/\)/g, ') ')
    .replace(/  +/g, ' ')
    .trim();
};

const RoadmapCard: React.FC<RoadmapCardProps> = ({ roadmap }) => {
  const [expandedSkill, setExpandedSkill] = useState<number | null>(null);
  const [expandedPhase, setExpandedPhase] = useState<number | null>(0);

  const getDisplayName = (name: string | null | undefined, description: string) => {
    if (name) return name;
    if (!description) return 'Untitled';
    // Extract a name from the first sentence of the description
    const firstSentence = description.split('.')[0].trim();
    return firstSentence || 'Untitled';
  };

  // Extract all skills from the roadmap
  const extractSkills = (): Skill[] => {
    const skills: Skill[] = [];

    // Make sure roadmap and phases exist
    if (!roadmap?.phases) return [];

    roadmap.phases.forEach((phase: Phase, phaseIndex: number) => {
      // Skip if phase has no topics
      if (!phase.topics || !Array.isArray(phase.topics)) return;

      phase.topics.forEach((topic: Topic, topicIndex: number) => {
        // Add topic as a skill if it has a name or description
        const topicName = topic.topicName || topic.name || '';
        const topicDesc = topic.description || topic.desc || '';
        
        if (topicName || topicDesc) {
          skills.push({
            name: getDisplayName(topicName, topicDesc),
            description: topicDesc,
            phaseIndex,
            topicIndex,
            resources: topic.resources || [],
            learningObjectives: topic.learningObjectives || topic.keyConcepts || []
          });
        }
        
        // Add subtopics as skills if they exist
        const subtopics = topic.subtopics || topic.items || [];
        subtopics.forEach((subtopic: Subtopic, subtopicIndex: number) => {
          const subtopicName = subtopic.name || subtopic.title || '';
          const subtopicDesc = subtopic.description || subtopic.desc || '';
          const learningObjs = subtopic.learningObjectives || 
                             subtopic.keyPoints || 
                             (subtopicDesc ? [subtopicDesc] : []);
          
          if (subtopicName || learningObjs.length > 0) {
            skills.push({
              name: getDisplayName(subtopicName, learningObjs[0] || ''),
              description: learningObjs.join('. '),
              phaseIndex,
              topicIndex,
              subtopicIndex,
              resources: subtopic.resources || [],
              learningObjectives: learningObjs
            });
          }
        });
      });
    });
    
    return skills;
  };

  const togglePhase = (index: number) => {
    setExpandedPhase(expandedPhase === index ? null : index);
  };

  const toggleSkill = (index: number) => {
    setExpandedSkill(expandedSkill === index ? null : index);
  };

  if (!roadmap) {
    return <div className="p-4 text-gray-500">No roadmap data available</div>;
  }

  const skills = extractSkills();
  const hasSkills = skills.length > 0;

  const getResourceIcon = (type: string = '') => {
    switch (type?.toLowerCase()) {
      case 'course':
        return <AcademicCapIcon className="h-4 w-4 mr-1 text-blue-500" />;
      case 'documentation':
        return <DocumentTextIcon className="h-4 w-4 mr-1 text-green-500" />;
      case 'video':
        return <VideoCameraIcon className="h-4 w-4 mr-1 text-purple-500" />;
      case 'code':
        return <CodeBracketIcon className="h-4 w-4 mr-1 text-yellow-500" />;
      default:
        return <BookOpenIcon className="h-4 w-4 mr-1 text-gray-500" />;
    }
  };

  return (
    <div className="space-y-8">
      {/* Skills Overview Section */}
      {hasSkills && (
        <div className="bg-white rounded-xl shadow-md overflow-hidden">
          <div className="p-6">
            <h2 className="text-2xl font-bold text-gray-900 mb-4 flex items-center">
              <span className="bg-blue-100 text-blue-800 p-2 rounded-lg mr-3">
                <BookOpenIcon className="h-6 w-6" />
              </span>
              Skills Overview
            </h2>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 mt-4">
              {skills.map((skill, index) => (
                <motion.div 
                  key={index}
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ duration: 0.3, delay: index * 0.05 }}
                  className={`p-4 rounded-lg border ${expandedSkill === index ? 'border-blue-400 bg-blue-50' : 'border-gray-200 hover:border-blue-300'} transition-all duration-200 cursor-pointer`}
                  onClick={() => setExpandedSkill(expandedSkill === index ? null : index)}
                >
                  <div className="flex justify-between items-start">
                    <h3 className="font-medium text-gray-900">{skill.name}</h3>
                    <span className="text-xs px-2 py-1 bg-blue-100 text-blue-800 rounded-full">
                      {skill.phaseIndex + 1}.{skill.topicIndex + 1}
                      {skill.subtopicIndex !== undefined ? `.${skill.subtopicIndex + 1}` : ''}
                    </span>
                  </div>
                  
                  <AnimatePresence>
                    {expandedSkill === index && (
                      <motion.div
                        initial={{ opacity: 0, height: 0 }}
                        animate={{ opacity: 1, height: 'auto' }}
                        exit={{ opacity: 0, height: 0 }}
                        transition={{ duration: 0.2 }}
                        className="mt-2 overflow-hidden"
                      >
                        {skill.description && (
                          <p className="text-sm text-gray-600 mb-3">{skill.description}</p>
                        )}
                        
                        {skill.learningObjectives.length > 0 && (
                          <div className="mb-3">
                            <h4 className="text-xs font-semibold text-gray-500 uppercase tracking-wider mb-1">
                              Learning Objectives
                            </h4>
                            <ul className="space-y-1">
                              {skill.learningObjectives.map((obj, i) => (
                                <li key={i} className="flex items-start">
                                  <span className="text-green-500 mr-1">✓</span>
                                  <span className="text-sm text-gray-700">{obj}</span>
                                </li>
                              ))}
                            </ul>
                          </div>
                        )}
                        
                        {skill.resources.length > 0 && (
                          <div>
                            <h4 className="text-xs font-semibold text-gray-500 uppercase tracking-wider mb-1">
                              Resources
                            </h4>
                            <ul className="space-y-1">
                              {skill.resources.map((resource, i) => (
                                <li key={i} className="text-sm">
                                  <a 
                                    href={resource.url} 
                                    target="_blank" 
                                    rel="noopener noreferrer"
                                    className="flex items-center text-blue-600 hover:text-blue-800 hover:underline"
                                    onClick={e => e.stopPropagation()}
                                  >
                                    {getResourceIcon(resource.type)}
                                    {resource.title || 'Resource'}
                                    {resource.type && (
                                      <span className="text-xs text-gray-500 ml-1">({resource.type})</span>
                                    )}
                                  </a>
                                </li>
                              ))}
                            </ul>
                          </div>
                        )}
                      </motion.div>
                    )}
                  </AnimatePresence>
                </motion.div>
              ))}
            </div>
          </div>
        </div>
      )}

      {/* Phases Section */}
      <div className="space-y-6">
        {roadmap.phases && roadmap.phases.length > 0 ? (
          roadmap.phases.map((phase: any, phaseIndex: number) => (
            <div 
              key={phaseIndex} 
              className="bg-white rounded-xl shadow-md overflow-hidden border border-gray-100 hover:shadow-lg transition-shadow duration-200"
            >
              <button
                className="w-full text-left p-6 focus:outline-none"
                onClick={() => togglePhase(phaseIndex)}
              >
                <div className="flex justify-between items-center">
                  <h2 className="text-xl font-bold text-gray-900 flex items-center">
                    <span className="bg-blue-100 text-blue-800 p-2 rounded-lg mr-3">
                      {phaseIndex + 1}
                    </span>
                    {phase.phaseName || `Phase ${phaseIndex + 1}`}
                  </h2>
                  <svg
                    className={`w-5 h-5 text-gray-500 transition-transform duration-200 ${expandedPhase === phaseIndex ? 'transform rotate-180' : ''}`}
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                  >
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                  </svg>
                </div>
                
                {phase.objective && (
                  <p className="mt-2 text-gray-600 pl-14">{phase.objective}</p>
                )}
                
                {phase.estimatedHours && (
                  <div className="mt-2 text-sm text-gray-500 pl-14">
                    Estimated time: {phase.estimatedHours} hours
                  </div>
                )}
              </button>
              
              <AnimatePresence>
                {expandedPhase === phaseIndex && (
                  <motion.div
                    initial={{ opacity: 0, height: 0 }}
                    animate={{ opacity: 1, height: 'auto' }}
                    exit={{ opacity: 0, height: 0 }}
                    transition={{ duration: 0.2 }}
                    className="overflow-hidden"
                  >
                    <div className="px-6 pb-6 pt-2">
                      {phase.topics && phase.topics.length > 0 ? (
                        phase.topics.map((topic: any, topicIndex: number) => {
                          const topicName = topic.topicName || topic.name || '';
                          const topicDesc = topic.description || topic.desc || '';
                          
                          return (
                            <div 
                              key={topicIndex} 
                              className="mt-4 p-4 bg-gray-50 rounded-lg border border-gray-100"
                            >
                              <h3 className="text-lg font-semibold text-gray-900 mb-2">
                                {getDisplayName(topicName, topicDesc)}
                              </h3>
                              
                              {topicDesc && (
                                <p className="text-gray-700 mb-3">{topicDesc}</p>
                              )}

                              {topic.estimatedHours && (
                                <div className="text-sm text-gray-500 mb-3 flex items-center">
                                  <svg className="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                                  </svg>
                                  {topic.estimatedHours} hours
                                </div>
                              )}

                              <div className="mt-3 space-y-4">
                                {topic.subtopics?.map((subtopic, subtopicIndex) => (
                                  <div 
                                    key={subtopicIndex} 
                                    className="p-3 bg-white rounded-md border border-gray-200 hover:border-blue-200 hover:bg-blue-50 transition-colors duration-150"
                                  >
                                    <h4 className="font-medium text-gray-900 mb-2">
                                      {getDisplayName(subtopic.name, subtopic.learningObjectives?.[0] || '')}
                                    </h4>
                                    
                                    {subtopic.learningObjectives && subtopic.learningObjectives.length > 0 && (
                                      <ul className="space-y-1.5 mb-3">
                                        {subtopic.learningObjectives.map((obj, i) => (
                                          <li key={i} className="flex items-start">
                                            <span className="text-blue-500 mr-2 mt-1">•</span>
                                            <span className="text-sm text-gray-700">{obj}</span>
                                          </li>
                                        ))}
                                      </ul>
                                    )}

                                    {subtopic.resources && subtopic.resources.length > 0 && (
                                      <div className="mt-2">
                                        <h5 className="text-xs font-semibold text-gray-500 uppercase tracking-wider mb-1">
                                          Resources
                                        </h5>
                                        <ul className="space-y-1">
                                          {subtopic.resources.map((resource, resIndex) => (
                                            <li key={resIndex} className="text-sm">
                                              <a 
                                                href={resource.url} 
                                                target="_blank" 
                                                rel="noopener noreferrer"
                                                className="flex items-center text-blue-600 hover:text-blue-800 hover:underline"
                                              >
                                                {getResourceIcon(resource.type)}
                                                {resource.title || 'Resource'}
                                                {resource.type && (
                                                  <span className="text-xs text-gray-500 ml-1">({resource.type})</span>
                                                )}
                                              </a>
                                            </li>
                                          ))}
                                        </ul>
                                      </div>
                                    )}
                                  </div>
                                ))}
                              </div>
                            </div>
                          );
                        })
                      ) : (
                        <p className="text-gray-500 text-center py-4">No topics available for this phase.</p>
                      )}
                    </div>
                  </motion.div>
                )}
              </AnimatePresence>
            </div>
          ))
        ) : (
          <div className="text-center py-8 text-gray-500">
            No phases available for this roadmap.
          </div>
        )}
      </div>
    </div>
  );
};

export default RoadmapCard;
