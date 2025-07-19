import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/router';
import Link from 'next/link';
import { ArrowLeftIcon } from '@heroicons/react/24/outline';
import Layout from '../../../../../components/Layout';
import TopicList from '../../../../../components/TopicList';
import ResourceLinks from '../../../../../components/ResourceLinks';
import QuestionAccordion from '../../../../../components/QuestionAccordion';
import LoadingSpinner from '../../../../../components/LoadingSpinner';
import { SkillRoadmap } from '../../../../../types';
import { fetchSkillRoadmap } from '../../../../../utils/api';

const SkillPage: React.FC = () => {
  const router = useRouter();
  const { role, experience, skill } = router.query;
  const [skillRoadmap, setSkillRoadmap] = useState<SkillRoadmap | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (role && experience && skill) {
      loadSkillRoadmap();
    }
  }, [role, experience, skill]);

  const loadSkillRoadmap = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await fetchSkillRoadmap(
        role as string, 
        experience as string, 
        skill as string
      );
      setSkillRoadmap(data);
    } catch (err) {
      setError('Failed to load skill roadmap. Please try again.');
      console.error('Error loading skill roadmap:', err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <Layout>
        <LoadingSpinner />
      </Layout>
    );
  }

  if (error) {
    return (
      <Layout>
        <div className="text-center py-12">
          <p className="text-red-600 mb-4">{error}</p>
          <button
            onClick={loadSkillRoadmap}
            className="btn-primary"
          >
            Try Again
          </button>
        </div>
      </Layout>
    );
  }

  if (!skillRoadmap) {
    return (
      <Layout>
        <div className="text-center py-12">
          <p className="text-gray-600">No skill roadmap found</p>
        </div>
      </Layout>
    );
  }

  // Format role and experience for the back link
  const roleSlug = role as string;
  const expSlug = experience as string;

  return (
    <Layout>
      <div className="max-w-4xl mx-auto">
        {/* Back Button */}
        <Link 
          href={`/roadmap/${roleSlug}/${expSlug}`}
          className="inline-flex items-center text-primary-600 hover:text-primary-800 mb-6"
        >
          <ArrowLeftIcon className="h-5 w-5 mr-1" />
          Back to Roadmap
        </Link>

        {/* Skill Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">
            {skillRoadmap.skill}
          </h1>
          <div className="prose max-w-none">
            <p className="text-gray-700">
              {skillRoadmap.roadmap}
            </p>
          </div>
        </div>

        {/* Topics */}
        <TopicList topics={skillRoadmap.topics} />

        {/* Resources */}
        <ResourceLinks resources={skillRoadmap.resources} />

        {/* Interview Questions */}
        <QuestionAccordion questions={skillRoadmap.interviewQuestions} />
      </div>
    </Layout>
  );
};

export default SkillPage;
