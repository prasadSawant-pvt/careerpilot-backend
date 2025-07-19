import React, { useState } from 'react';
import { useRouter } from 'next/router';
import { ArrowPathIcon } from '@heroicons/react/24/outline';

const roles = [
  'Java Backend Developer',
  'Frontend Developer',
  'Full Stack Developer',
  'Data Scientist',
  'DevOps Engineer',
  'Mobile Developer',
  'UI/UX Designer',
  'QA Engineer',
  'Cloud Architect',
  'Machine Learning Engineer'
];

const experiences = [
  { value: '0-1yr', label: 'Entry Level (0-1 year)' },
  { value: '1-2yrs', label: 'Junior (1-2 years)' },
  { value: '2-3yrs', label: 'Mid-Level (2-3 years)' },
  { value: '3-5yrs', label: 'Senior (3-5 years)' },
  { value: '5+yrs', label: 'Lead/Architect (5+ years)' }
];

const RoleForm: React.FC = () => {
  const [selectedRole, setSelectedRole] = useState('');
  const [selectedExperience, setSelectedExperience] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const router = useRouter();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    
    if (!selectedRole) {
      setError('Please select a role');
      return;
    }
    
    if (!selectedExperience) {
      setError('Please select an experience level');
      return;
    }

    setIsLoading(true);
    
    try {
      // Convert to URL-friendly format
      const roleSlug = selectedRole.toLowerCase().replace(/\s+/g, '-');
      const expSlug = selectedExperience.toLowerCase();
      
      await router.push(`/roadmap/${roleSlug}/${expSlug}`);
    } catch (err) {
      console.error('Navigation error:', err);
      setError('Failed to load roadmap. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div className="text-center mb-12">
        <h1 className="text-4xl font-extrabold text-gray-900 mb-4 sm:text-5xl">
          AI-Powered Career Roadmap
        </h1>
        <p className="text-xl text-gray-600 max-w-2xl mx-auto">
          Get a personalized learning path tailored to your target role and experience level
        </p>
      </div>
      
      <div className="bg-white shadow-lg rounded-xl p-6 sm:p-8 max-w-2xl mx-auto">
        <form onSubmit={handleSubmit} className="space-y-6">
          {error && (
            <div className="bg-red-50 border-l-4 border-red-400 p-4 rounded">
              <div className="flex">
                <div className="flex-shrink-0">
                  <svg className="h-5 w-5 text-red-400" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                    <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                  </svg>
                </div>
                <div className="ml-3">
                  <p className="text-sm text-red-700">{error}</p>
                </div>
              </div>
            </div>
          )}
          
          <div className="space-y-2">
            <label htmlFor="role" className="block text-sm font-medium text-gray-700">
              What role are you targeting?
            </label>
            <select
              id="role"
              value={selectedRole}
              onChange={(e) => setSelectedRole(e.target.value)}
              className="mt-1 block w-full pl-3 pr-10 py-3 text-base border-gray-300 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm rounded-lg border"
              disabled={isLoading}
            >
              <option value="">Select a role</option>
              {roles.map((role) => (
                <option key={role} value={role}>
                  {role}
                </option>
              ))}
            </select>
          </div>
          
          <div className="space-y-2">
            <label htmlFor="experience" className="block text-sm font-medium text-gray-700">
              What's your target experience level?
            </label>
            <select
              id="experience"
              value={selectedExperience}
              onChange={(e) => setSelectedExperience(e.target.value)}
              className="mt-1 block w-full pl-3 pr-10 py-3 text-base border-gray-300 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm rounded-lg border"
              disabled={isLoading}
            >
              <option value="">Select experience level</option>
              {experiences.map((exp) => (
                <option key={exp.value} value={exp.value}>
                  {exp.label}
                </option>
              ))}
            </select>
          </div>
          
          <div className="pt-2">
            <button
              type="submit"
              disabled={isLoading || !selectedRole || !selectedExperience}
              className={`w-full flex justify-center items-center py-3 px-6 border border-transparent rounded-lg shadow-sm text-base font-medium text-white ${
                isLoading || !selectedRole || !selectedExperience
                  ? 'bg-blue-400 cursor-not-allowed'
                  : 'bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-colors duration-200'
              }`}
            >
              {isLoading ? (
                <>
                  <ArrowPathIcon className="animate-spin -ml-1 mr-2 h-5 w-5 text-white" />
                  Generating...
                </>
              ) : (
                'Generate My Roadmap'
              )}
            </button>
          </div>
          
          <p className="text-xs text-gray-500 text-center mt-4">
            We'll create a personalized learning path based on your selections
          </p>
        </form>
      </div>
      
      <div className="mt-16 grid grid-cols-1 gap-8 sm:grid-cols-2 lg:grid-cols-3">
        <div className="bg-white p-6 rounded-lg shadow-md border border-gray-100">
          <div className="text-blue-600 mb-3">
            <svg className="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
            </svg>
          </div>
          <h3 className="text-lg font-medium text-gray-900 mb-2">Structured Learning</h3>
          <p className="text-gray-600 text-sm">Follow a clear, step-by-step path to master your target role.</p>
        </div>
        
        <div className="bg-white p-6 rounded-lg shadow-md border border-gray-100">
          <div className="text-blue-600 mb-3">
            <svg className="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
            </svg>
          </div>
          <h3 className="text-lg font-medium text-gray-900 mb-2">Curated Resources</h3>
          <p className="text-gray-600 text-sm">Access the best learning materials and resources in one place.</p>
        </div>
        
        <div className="bg-white p-6 rounded-lg shadow-md border border-gray-100">
          <div className="text-blue-600 mb-3">
            <svg className="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
            </svg>
          </div>
          <h3 className="text-lg font-medium text-gray-900 mb-2">Save Time</h3>
          <p className="text-gray-600 text-sm">Focus on what matters most with our efficient learning paths.</p>
        </div>
      </div>
    </div>
  );
};

export default RoleForm;
