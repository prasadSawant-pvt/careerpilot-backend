import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/router';
import Layout from '../../../components/Layout';
import RoadmapCard from '../../../components/RoadmapCard';
import LoadingSpinner from '../../../components/LoadingSpinner';
import { RoleRoadmap } from '../../../types';

// Mock data for demonstration
const MOCK_ROADMAPS: Record<string, Record<string, RoleRoadmap>> = {
  'java-backend-developer': {
    '2-3yrs': {
      id: 'java-2-3yrs',
      role: 'Java Backend Developer',
      experience: '2-3 years',
      overview: 'Roadmap for Java Backend Developers with 2-3 years of experience',
      skills: [
        {
          id: 'spring-boot',
          name: 'Spring Boot',
          description: 'Build production-ready applications with Spring Boot',
          level: 'Intermediate',
          completed: false,
          resources: [
            { name: 'Spring Boot Documentation', url: 'https://spring.io/projects/spring-boot' },
            { name: 'Spring Boot Tutorial', url: 'https://www.baeldung.com/spring-boot' }
          ]
        },
        {
          id: 'hibernate',
          name: 'Hibernate',
          description: 'Object-relational mapping for Java',
          level: 'Intermediate',
          completed: false,
          resources: [
            { name: 'Hibernate Documentation', url: 'https://hibernate.org/orm/documentation/' },
            { name: 'Hibernate Tutorial', url: 'https://www.baeldung.com/hibernate-5-bootstrapping-api' }
          ]
        }
      ],
      resources: [
        { name: 'Java Documentation', url: 'https://docs.oracle.com/en/java/' },
        { name: 'Spring Framework', url: 'https://spring.io/projects/spring-framework' }
      ]
    }
  }
};

const RoadmapPage: React.FC = () => {
  const router = useRouter();
  const { role, experience } = router.query;
  const [roadmap, setRoadmap] = useState<RoleRoadmap | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (role && experience) {
      loadMockData(role as string, experience as string);
    }
  }, [role, experience]);

  const loadMockData = (roleId: string, expLevel: string) => {
    setLoading(true);
    setError(null);
    
    // Simulate API call delay
    setTimeout(() => {
      try {
        const roleData = MOCK_ROADMAPS[roleId];
        if (roleData && roleData[expLevel]) {
          setRoadmap(roleData[expLevel]);
        } else {
          setError('No roadmap found for the selected role and experience level.');
        }
      } catch (err) {
        setError('Failed to load roadmap data.');
        console.error('Error loading roadmap:', err);
      } finally {
        setLoading(false);
      }
    }, 500);
  };

  if (loading) {
    return (
      <Layout>
        <div className="min-h-screen flex items-center justify-center">
          <LoadingSpinner />
        </div>
      </Layout>
    );
  }

  if (error) {
    return (
      <Layout>
        <div className="max-w-4xl mx-auto px-4 py-12">
          <div className="text-center py-12">
            <h1 className="text-2xl font-bold text-gray-900 mb-4">Error Loading Roadmap</h1>
            <p className="text-red-600 mb-6">{error}</p>
            <button
              onClick={() => loadMockData(role as string, experience as string)}
              className="btn-primary"
            >
              Try Again
            </button>
          </div>
        </div>
      </Layout>
    );
  }

  if (!roadmap) {
    return (
      <Layout>
        <div className="max-w-4xl mx-auto px-4 py-12">
          <div className="text-center py-12">
            <h1 className="text-2xl font-bold text-gray-900 mb-4">No Roadmap Found</h1>
            <p className="text-gray-600">We couldn't find a roadmap for the selected criteria.</p>
          </div>
        </div>
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="max-w-4xl mx-auto px-4 py-8">
        <RoadmapCard roadmap={roadmap} />
      </div>
    </Layout>
  );
};

export default RoadmapPage;
