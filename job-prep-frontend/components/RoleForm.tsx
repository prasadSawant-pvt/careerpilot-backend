import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/router';
import { ArrowPathIcon } from '@heroicons/react/24/outline';
import { useApi } from '@/hooks/useApi';
import ApiService from '@/services/api';
import { Role, ExperienceLevel } from '@/types/api';

// Static roles that will always be available
const STATIC_ROLES: Role[] = [
  { id: 'frontend', name: 'Frontend Developer' },
  { id: 'backend', name: 'Backend Developer' },
  { id: 'fullstack', name: 'Full Stack Developer' },
  { id: 'devops', name: 'DevOps Engineer' },
  { id: 'data-scientist', name: 'Data Scientist' },
  { id: 'ai-ml-engineer', name: 'AI/ML Engineer' },
  { id: 'qa-engineer', name: 'QA Engineer' },
  { id: 'mobile', name: 'Mobile Developer' },
  { id: 'cloud-architect', name: 'Cloud Architect' },
  { id: 'security-engineer', name: 'Security Engineer' },
  { id: 'data-engineer', name: 'Data Engineer' },
  { id: 'sre', name: 'Site Reliability Engineer' },
  { id: 'other', name: 'Other (Specify)' },
];

// Fallback roles in case the API is not available
const FALLBACK_ROLES: Role[] = [...STATIC_ROLES];

const RoleForm: React.FC = () => {
  const [selectedRole, setSelectedRole] = useState('');
  const [selectedExperience, setSelectedExperience] = useState('');
  const [customRole, setCustomRole] = useState('');
  const [showCustomRoleInput, setShowCustomRoleInput] = useState(false);
  const [roles, setRoles] = useState<Role[]>([]);
  
  // Hardcoded experience levels
  const experiences: ExperienceLevel[] = [
    { id: 'intern', name: 'Intern' },
    { id: 'beginner', name: 'Beginner (0-2 years)' },
    { id: 'intermediate', name: 'Intermediate (2-5 years)' },
    { id: 'senior', name: 'Senior (5+ years)' },
    { id: 'lead', name: 'Lead/Manager' }
  ];
  
  const [isLoading, setIsLoading] = useState(false);
  const [isInitializing, setIsInitializing] = useState(true);
  const [error, setError] = useState('');
  const router = useRouter();
  const { execute: fetchRoles } = useApi<Role[]>();
  
  // Handle role selection change
  const handleRoleChange = (e: React.ChangeEvent<{ value: unknown }>) => {
    const roleId = e.target.value as string;
    setSelectedRole(roleId);
    
    // Show custom role input if "Other" is selected
    if (roleId === 'other') {
      setShowCustomRoleInput(true);
      setCustomRole('');
    } else {
      setShowCustomRoleInput(false);
      setCustomRole('');
    }
  };

  // Fetch roles on component mount
  useEffect(() => {
    const initializeData = async () => {
      try {
        setIsInitializing(true);
        setError('');
        
        try {
          // Try to fetch roles from the API
          const rolesResponse = await fetchRoles(() => ApiService.getRoles());
          if (rolesResponse?.success && rolesResponse.data?.length) {
            setRoles(rolesResponse.data);
            return; // Successfully loaded roles from API
          }
          
          // If API call fails or returns no data, use fallback roles
          console.warn('No roles found from API, using fallback roles');
          setRoles(FALLBACK_ROLES);
        } catch (err) {
          // If API call fails, use fallback roles
          console.warn('Failed to fetch roles from API, using fallback roles:', err);
          setRoles(FALLBACK_ROLES);
        }
      } catch (err) {
        console.error('Error initializing roles:', err);
        // Even if there's an error, we can still show the form with fallback roles
        setRoles(FALLBACK_ROLES);
        setError('Some features may be limited. Using default roles.');
      } finally {
        setIsInitializing(false);
      }
    };
    
    initializeData();
  }, [fetchRoles]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    
    // Validate form
    if (!selectedRole) {
      setError('Please select a role');
      return;
    }
    
    if (selectedRole === 'other' && !customRole.trim()) {
      setError('Please enter a custom role');
      return;
    }
    
    if (!selectedExperience) {
      setError('Please select an experience level');
      return;
    }

    setIsLoading(true);
    
    try {
      // For custom roles, use the entered text, otherwise use the selected role from the list
      let roleToUse;
      if (selectedRole === 'other') {
        roleToUse = { 
          id: customRole.toLowerCase().replace(/\s+/g, '-'), 
          name: customRole 
        };
      } else {
        // For predefined roles, find the role in the static list first, then fall back to API roles
        roleToUse = STATIC_ROLES.find(r => r.id === selectedRole) || 
                   roles.find(r => r.id === selectedRole);
      }
      
      const experience = experiences.find(e => e.id === selectedExperience);
      
      if (!roleToUse || !experience) {
        throw new Error('Invalid selection');
      }
      
      // Create URL-friendly slugs for the route
      // Use the role name for the API call but keep the ID in the URL for better readability
      const roleSlug = roleToUse.id;
      const experienceSlug = experience.id;
      
      // Navigate to the roadmap page with the selected role and experience
      // The API call in the roadmap page will use the role name from the route parameters
      router.push(`/roadmap/${encodeURIComponent(roleToUse.name)}/${experienceSlug}`);
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
          
          <div className="mb-6">
            <label htmlFor="role" className="block text-sm font-medium text-gray-700 mb-2">
              Select Your Role
            </label>
            <select
              id="role"
              value={selectedRole}
              onChange={handleRoleChange}
              className="w-full px-4 py-3 rounded-lg border border-gray-300 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200"
              disabled={isLoading || isInitializing}
            >
              <option value="">Select a role</option>
              {STATIC_ROLES.map((role) => (
                <option key={role.id} value={role.id}>
                  {role.name}
                </option>
              ))}
            </select>
            
            {/* Custom role input */}
            {showCustomRoleInput && (
              <div className="mt-3">
                <label htmlFor="customRole" className="block text-sm font-medium text-gray-700 mb-1">
                  Enter your specific role
                </label>
                <input
                  type="text"
                  id="customRole"
                  value={customRole}
                  onChange={(e) => setCustomRole(e.target.value)}
                  className="w-full px-4 py-3 rounded-lg border border-gray-300 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200"
                  placeholder="E.g., Blockchain Developer, Game Developer, etc."
                  disabled={isLoading}
                />
              </div>
            )}
            {isInitializing && (
              <p className="mt-2 text-sm text-gray-500">Loading roles...</p>
            )}
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
              disabled={isLoading || isInitializing || !selectedRole}
            >
              <option value="">Select experience level</option>
              {experiences.map((exp) => (
                <option key={exp.id} value={exp.id}>
                  {exp.name}
                </option>
              ))}
            </select>
            {isInitializing && (
              <p className="mt-2 text-sm text-gray-500">Loading experience levels...</p>
            )}
          </div>
          
          <div className="pt-2">
            <button
              type="submit"
              disabled={isLoading || isInitializing}
              className={`w-full flex justify-center py-3 px-4 border border-transparent rounded-lg shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 ${isLoading || isInitializing ? 'opacity-75 cursor-not-allowed' : ''}`}
            >
              {isLoading ? (
                <>
                  <ArrowPathIcon className="animate-spin -ml-1 mr-2 h-5 w-5 text-white" />
                  Loading...
                </>
              ) : isInitializing ? (
                'Loading form data...'
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
