import React, { useEffect } from 'react';
import { useCareerPath } from '../contexts/CareerPathContext';
import { Button } from './ui/button';

export const RoleSelector: React.FC = () => {
  const {
    roles = [],
    experiences = [],
    selectedRole,
    selectedExperience,
    isLoading = false,
    error = null,
    fetchRoles = () => {},
    fetchExperiences = () => {},
    selectRole = () => {},
    selectExperience = () => {},
    fetchRoadmap = () => {},
  } = useCareerPath() || {};

  // Fetch initial data
  useEffect(() => {
    fetchRoles();
    fetchExperiences();
  }, [fetchRoles, fetchExperiences]);

  // Fetch roadmap when both role and experience are selected
  useEffect(() => {
    if (selectedRole && selectedExperience) {
      fetchRoadmap(selectedRole.id, selectedExperience.id);
    }
  }, [selectedRole, selectedExperience, fetchRoadmap]);

  if (isLoading && !selectedRole) {
    return <div className="text-center py-8">Loading roles and experience levels...</div>;
  }

  if (error) {
    return (
      <div className="text-red-600 p-4 bg-red-50 rounded-lg">
        Error: {error.message || 'Failed to load data'}
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-lg font-semibold mb-2">Select Your Role</h2>
        <div className="flex flex-wrap gap-2">
          {roles.map((role: { id: string; name: string }) => (
            <Button
              key={role.id}
              variant={selectedRole?.id === role.id ? 'default' : 'outline'}
              onClick={() => selectRole(role)}
            >
              {role.name}
            </Button>
          ))}
        </div>
      </div>

      {selectedRole && (
        <div>
          <h2 className="text-lg font-semibold mb-2">Select Your Experience Level</h2>
          <div className="flex flex-wrap gap-2">
            {experiences.map((exp: { id: string; name: string }) => (
              <Button
                key={exp.id}
                variant={selectedExperience?.id === exp.id ? 'default' : 'outline'}
                onClick={() => selectExperience(exp)}
              >
                {exp.name}
              </Button>
            ))}
          </div>
        </div>
      )}

      {isLoading && selectedRole && (
        <div className="text-center py-4">Loading roadmap...</div>
      )}
    </div>
  );
};

export default RoleSelector;
