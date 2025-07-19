import React from 'react';
import { Resource } from '../types';
import { LinkIcon } from '@heroicons/react/24/outline';

interface ResourceLinksProps {
  resources: Resource[];
}

const ResourceLinks: React.FC<ResourceLinksProps> = ({ resources }) => {
  return (
    <div className="mb-8">
      <h3 className="text-xl font-semibold text-gray-900 mb-4">
        Curated Resources
      </h3>
      <div className="space-y-3">
        {resources.map((resource, index) => (
          <a
            key={index}
            href={resource.url}
            target="_blank"
            rel="noopener noreferrer"
            className="flex items-center text-blue-600 hover:text-blue-800 hover:underline"
          >
            <LinkIcon className="h-4 w-4 mr-2 flex-shrink-0" />
            {resource.title}
          </a>
        ))}
      </div>
    </div>
  );
};

export default ResourceLinks;
