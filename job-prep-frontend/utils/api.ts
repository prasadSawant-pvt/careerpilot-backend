const API_BASE_URL = process.env.NEXT_PUBLIC_BACKEND_URL || 'http://localhost:3001';

export async function fetchRoleRoadmap(role: string, experience: string) {
  try {
    const response = await fetch(`${API_BASE_URL}/api/roadmap/${role}/${experience}`);
    if (!response.ok) {
      throw new Error('Failed to fetch roadmap');
    }
    return await response.json();
  } catch (error) {
    console.error('Error fetching role roadmap:', error);
    throw error;
  }
}

export async function fetchSkillRoadmap(role: string, experience: string, skill: string) {
  try {
    const response = await fetch(`${API_BASE_URL}/api/skill/roadmap/${role}/${experience}/${skill}`);
    if (!response.ok) {
      throw new Error('Failed to fetch skill roadmap');
    }
    return await response.json();
  } catch (error) {
    console.error('Error fetching skill roadmap:', error);
    throw error;
  }
}
