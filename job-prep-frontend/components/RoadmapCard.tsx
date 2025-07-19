import React from 'react';
import { RoleRoadmap, Skill } from '../types';
import Link from 'next/link';

interface RoadmapCardProps {
  roadmap: RoleRoadmap;
}

const RoadmapCard: React.FC<RoadmapCardProps> = ({ roadmap }) => {
  return (
    <div className="space-y-8">
      <div className="bg-white rounded-lg shadow-md p-6 border border-gray-100">
        <div className="mb-6">
          <h1 className="text-3xl font-bold text-gray-900">
            {roadmap.role} Roadmap
          </h1>
          <p className="text-lg text-gray-600 mt-2">
            {roadmap.experience} Experience
          </p>
        </div>
        
        <div className="prose max-w-none text-gray-700 mb-6">
          <p>{roadmap.overview}</p>
        </div>

        {roadmap.resources && roadmap.resources.length > 0 && (
          <div className="mt-6">
            <h3 className="text-lg font-medium text-gray-900 mb-3">General Resources</h3>
            <ul className="space-y-2">
              {roadmap.resources.map((resource, index) => (
                <li key={index}>
                  <a 
                    href={resource.url} 
                    target="_blank" 
                    rel="noopener noreferrer"
                    className="text-blue-600 hover:text-blue-800 hover:underline"
                  >
                    {resource.name}
                  </a>
                </li>
              ))}
            </ul>
          </div>
        )}
      </div>

      <div>
        <h2 className="text-2xl font-semibold text-gray-900 mb-6">
          Key Skills to Master
        </h2>
        <div className="grid gap-6 md:grid-cols-1 lg:grid-cols-2">
          {roadmap.skills.map((skill: Skill) => (
            <div 
              key={skill.id}
              className="bg-white rounded-lg shadow-md p-6 border border-gray-100 hover:shadow-lg transition-shadow duration-200"
            >
              <h3 className="text-xl font-semibold text-gray-900 mb-2">{skill.name}</h3>
              <p className="text-gray-600 mb-4">{skill.description}</p>
              <div className="flex items-center text-sm text-gray-500 mb-4">
                <span className="px-3 py-1 bg-blue-100 text-blue-800 rounded-full text-xs font-medium">
                  {skill.level}
                </span>
              </div>
              
              {skill.resources && skill.resources.length > 0 && (
                <div className="mt-4">
                  <h4 className="font-medium text-gray-900 mb-2">Resources:</h4>
                  <ul className="space-y-2">
                    {skill.resources.map((resource, idx) => (
                      <li key={idx}>
                        <a 
                          href={resource.url} 
                          target="_blank" 
                          rel="noopener noreferrer"
                          className="text-blue-600 hover:text-blue-800 hover:underline text-sm"
                        >
                          {resource.name}
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
    </div>
  );
};

export default RoadmapCard;
