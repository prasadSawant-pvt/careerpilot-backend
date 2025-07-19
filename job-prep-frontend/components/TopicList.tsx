import React from 'react';
import { CheckIcon } from '@heroicons/react/24/solid';

interface TopicListProps {
  topics: string[];
}

const TopicList: React.FC<TopicListProps> = ({ topics }) => {
  return (
    <div className="mb-8">
      <h3 className="text-xl font-semibold text-gray-900 mb-4">
        Topics to Study
      </h3>
      <ul className="space-y-3">
        {topics.map((topic, index) => (
          <li key={index} className="flex items-start">
            <CheckIcon className="h-5 w-5 text-green-500 mr-2 mt-0.5 flex-shrink-0" />
            <span className="text-gray-700">{topic}</span>
          </li>
        ))}
      </ul>
    </div>
  );
};

export default TopicList;
