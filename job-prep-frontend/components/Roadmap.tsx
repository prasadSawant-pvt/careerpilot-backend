import React from 'react';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { CheckCircle, Circle, ExternalLink } from 'lucide-react';
import { Button } from './ui/button';
import { useCareerPath } from '../contexts/CareerPathContext';

export const Roadmap: React.FC = () => {
  const { 
    roadmap = null, 
    isLoading = false, 
    selectedRole = null, 
    selectedExperience = null 
  } = useCareerPath() || {};

  interface RoadmapSection {
    id: string;
    title: string;
    items: string[];
  }

  interface Resource {
    type: string;
    title: string;
    url: string;
    provider?: string;
  }

  if (!selectedRole || !selectedExperience) {
    return (
      <div className="text-center py-12 bg-muted/50 rounded-lg">
        <h3 className="text-lg font-medium text-muted-foreground">
          Please select a role and experience level to view the roadmap
        </h3>
      </div>
    );
  }

  if (isLoading) {
    return (
      <div className="space-y-4">
        <div className="h-8 bg-muted rounded w-1/3 animate-pulse" />
        <div className="space-y-2">
          {[1, 2, 3, 4].map((i) => (
            <div key={i} className="h-4 bg-muted rounded w-full animate-pulse" />
          ))}
        </div>
      </div>
    );
  }

  if (!roadmap) {
    return (
      <div className="text-center py-12 bg-muted/50 rounded-lg">
        <h3 className="text-lg font-medium text-muted-foreground">
          No roadmap available for the selected role and experience level
        </h3>
      </div>
    );
  }

  return (
    <div className="space-y-8">
      <div className="space-y-2">
        <h1 className="text-3xl font-bold tracking-tight">
          {roadmap.role} Roadmap
        </h1>
        <p className="text-muted-foreground">
          {roadmap.experience} • Estimated time: {roadmap.timeline}
        </p>
      </div>

      <div className="space-y-6">
        {roadmap?.roadmap?.map((section: RoadmapSection) => (
          <Card key={section.id} className="overflow-hidden">
            <CardHeader className="bg-muted/50 border-b">
              <CardTitle className="text-xl">{section.title}</CardTitle>
            </CardHeader>
            <CardContent className="p-6">
              <ul className="space-y-3">
                {section.items.map((item: string, index: number) => (
                  <li key={index} className="flex items-start gap-3">
                    <div className="flex-shrink-0 mt-1">
                      <Circle className="h-4 w-4 text-muted-foreground" />
                    </div>
                    <span className="text-foreground">{item}</span>
                  </li>
                ))}
              </ul>
            </CardContent>
          </Card>
        ))}
      </div>

      {roadmap.resources && roadmap.resources.length > 0 && (
        <div className="mt-8">
          <h2 className="text-xl font-semibold mb-4">Recommended Resources</h2>
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
            {roadmap.resources.map((resource: Resource, index: number) => (
              <Card key={index} className="h-full flex flex-col">
                <CardHeader className="pb-2">
                  <div className="flex items-center justify-between">
                    <span className="text-sm font-medium text-muted-foreground">
                      {resource.type.charAt(0).toUpperCase() + resource.type.slice(1)}
                    </span>
                    {resource.provider && (
                      <span className="text-xs px-2 py-1 bg-muted rounded-full">
                        {resource.provider}
                      </span>
                    )}
                  </div>
                  <CardTitle className="text-lg mt-2">{resource.title}</CardTitle>
                </CardHeader>
                <CardContent className="pt-0 mt-auto">
                  <Button variant="outline" size="sm" asChild className="w-full">
                    <a 
                      href={resource.url} 
                      target="_blank" 
                      rel="noopener noreferrer"
                      className="flex items-center gap-2"
                    >
                      View Resource
                      <ExternalLink className="h-4 w-4" />
                    </a>
                  </Button>
                </CardContent>
              </Card>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

export default Roadmap;
