import React from 'react';
import Link from 'next/link';
import { ArrowRightIcon } from '@heroicons/react/24/outline';
import { Skill } from '../types';

interface SkillCardProps {
  skill: Skill;
  role: string;
  experience: string;
  index: number;
}

const SkillCard: React.FC<SkillCardProps> = ({ skill, role, experience, index }) => {
  const roleSlug = role.toLowerCase().replace(/\s+/g, '-');
  const expSlug = experience.toLowerCase();
  const skillSlug = skill.name.toLowerCase().replace(/\s+/g, '-');

  return (
    <Link href={`/skill/${roleSlug}/${expSlug}/${skillSlug}`}>
      <div className="h-full bg-white rounded-lg shadow-md p-6 border border-gray-100 hover:shadow-lg transition-shadow duration-200 cursor-pointer">
        <div className="flex flex-col h-full">
          <div className="flex-grow">
            <div className="flex items-center mb-3">
              <span className="inline-flex items-center justify-center w-8 h-8 rounded-full bg-blue-100 text-blue-800 font-medium text-sm mr-3">
                {index + 1}
              </span>
              <h3 className="text-xl font-semibold text-gray-900">
                {skill.name}
              </h3>
            </div>
            
            <p className="text-gray-600 mb-4">
              {skill.description}
            </p>
            
            <div className="flex items-center text-sm text-gray-500 mb-4">
              <span className="px-3 py-1 bg-blue-100 text-blue-800 rounded-full text-xs font-medium">
                {skill.level}
              </span>
            </div>
            
            {skill.resources && skill.resources.length > 0 && (
              <div className="mt-3">
                <p className="text-sm text-gray-500 mb-2">Resources available:</p>
                <ul className="space-y-1">
                  {skill.resources.slice(0, 2).map((resource, idx) => (
                    <li key={idx} className="text-sm text-blue-600 hover:underline truncate">
                      {resource.name}
                    </li>
                  ))}
                  {skill.resources.length > 2 && (
                    <li className="text-sm text-gray-500">+{skill.resources.length - 2} more</li>
                  )}
                </ul>
              </div>
            )}
          </div>
          
          <div className="mt-4 flex items-center text-blue-600 text-sm font-medium">
            View details
            <ArrowRightIcon className="h-4 w-4 ml-1" />
          </div>
        </div>
      </div>
    </Link>
  );
};

export default SkillCard;
