import React, { useEffect, useState, useCallback } from 'react';
import { useRouter } from 'next/router';
import { Button, Select, MenuItem, FormControl, InputLabel, Box, Typography, Grid, CircularProgress } from '@mui/material';
import Layout from '../../../components/Layout';
import LoadingSpinner from '../../../components/LoadingSpinner';
import RoadmapCard from '../../../components/RoadmapCard';
import { useApi } from '../../../hooks/useApi';
import ApiService from '../../../services/api';
import { DetailedRoadmapResponse } from '../../../types/api';

const RoadmapPage: React.FC = () => {
  const router = useRouter();
  const { role, experience } = router.query;
  const [roadmap, setRoadmap] = useState<DetailedRoadmapResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [timelineWeeks, setTimelineWeeks] = useState<number>(12); // Default to 12 weeks
  const [isRegenerating, setIsRegenerating] = useState(false);
  const { execute: fetchRoadmap } = useApi<DetailedRoadmapResponse>();

  const loadRoadmap = useCallback(async (weeks?: number, forceRegenerate: boolean = false) => {
    if (!role || !experience) {
      setError('Invalid role or experience level');
      setLoading(false);
      return;
    }

    const isRegeneration = weeks !== undefined;
    
    if (isRegeneration) {
      setIsRegenerating(true);
    } else {
      setLoading(true);
    }
    setError(null);

    try {
      let response;
      
      // Decode the role name from the URL
      const roleName = typeof role === 'string' ? decodeURIComponent(role) : '';
      const experienceLevel = typeof experience === 'string' ? experience : '';
      
      if (isRegeneration) {
        // Use POST /roadmaps/detailed for regeneration with forceRegenerate=true
        response = await fetchRoadmap(() => 
          ApiService.regenerateDetailedRoadmap(
            roleName, 
            experienceLevel,
            undefined, // currentSkills
            weeks || timelineWeeks, // Use provided weeks or current state
            undefined, // focusArea
            forceRegenerate // Force regeneration from the server
          )
        );
      } else {
        // Use GET /roadmaps/detailed for initial load with forceRegenerate=false
        response = await fetchRoadmap(() => 
          ApiService.getDetailedRoadmap(
            roleName, 
            experienceLevel,
            undefined, // currentSkills
            timelineWeeks, // Use current timelineWeeks for initial load
            undefined, // focusArea
            false // Don't force regeneration on initial load
          )
        );
      }

      if (response?.success && response.data) {
        setRoadmap(response.data);
        // Update the timelineWeeks state if we're regenerating with a new value
        if (weeks !== undefined) {
          setTimelineWeeks(weeks);
        }
      } else {
        // Check if the error is due to role/experience not found
        if (response?.statusCode === 404) {
          setError('No roadmap found for the selected role and experience level.');
        } else {
          setError(response?.message || 'Failed to load roadmap. Please try again.');
        }
      }
    } catch (err) {
      console.error('Error loading roadmap:', err);
      setError('An unexpected error occurred while loading the roadmap.');
    } finally {
      if (weeks !== undefined) {
        setIsRegenerating(false);
      } else {
        setLoading(false);
      }
    }
  }, [role, experience, fetchRoadmap, timelineWeeks]);

  // Handle timeline weeks change
  const handleTimelineWeeksChange = (
    event: React.ChangeEvent<{ name?: string; value: unknown }> | { target: { value: number } },
    child?: React.ReactNode
  ) => {
    const weeks = Number('target' in event ? event.target.value : event.value);
    if (!isNaN(weeks)) {
      setTimelineWeeks(weeks);
      // Force a fresh generation with the new timeline
      loadRoadmap(weeks, true);
    }
  };

  useEffect(() => {
    if (router.isReady) {
      loadRoadmap();
    }
  }, [router.isReady, loadRoadmap]);

  const handleRetry = () => {
    loadRoadmap();
  };

  if (loading) {
    return (
      <Layout title="Loading Roadmap...">
        <Box display="flex" justifyContent="center" alignItems="center" minHeight="60vh">
          <LoadingSpinner />
        </Box>
      </Layout>
    );
  }

  if (error) {
    return (
      <Layout title="Error">
        <Box textAlign="center" p={4}>
          <Typography variant="h6" color="error" gutterBottom>
            {error}
          </Typography>
          <Button 
            variant="contained" 
            color="primary" 
            onClick={() => loadRoadmap()}
            sx={{ mt: 2 }}
          >
            Retry
          </Button>
        </Box>
      </Layout>
    );
  }

  if (!roadmap) {
    return (
      <Layout title="No Roadmap Found">
        <div className="max-w-4xl mx-auto px-4 py-12">
          <div className="text-center py-12">
            <h1 className="text-2xl font-bold text-gray-900 mb-4">No Roadmap Found</h1>
            <p className="text-gray-600">We couldn't find a roadmap for the selected criteria.</p>
            <button
              onClick={() => router.push('/')}
              className="mt-4 px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
            >
              Back to Home
            </button>
          </div>
        </div>
      </Layout>
    );
  }

  // Generate week options (4 to 52 weeks, in steps of 2)
  const weekOptions = Array.from({ length: 25 }, (_, i) => (i + 1) * 2);

  // Format role name for display (e.g., "software-engineer" -> "Software Engineer")
  const formatRoleName = (role: string | string[] | undefined) => {
    if (!role) return 'Loading...';
    const roleStr = Array.isArray(role) ? role[0] : role;
    // Decode the role name and format it nicely
    return decodeURIComponent(roleStr)
      .split(/[\s-]+/)
      .map(word => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' ');
  };

  // Format experience level for display (e.g., "intermediate-(2-5-years)" -> "Intermediate (2-5 years)")
  const formatExperience = (exp: string | string[] | undefined) => {
    if (!exp) return '';
    const expStr = Array.isArray(exp) ? exp[0] : exp;
    // Convert from "intermediate-(2-5-years)" to "Intermediate (2-5 years)"
    return expStr
      .split('-')
      .map(word => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' ')
      .replace(/\(/g, ' (')
      .replace(/\)/g, ') ')
      .replace(/\s+/g, ' ')
      .trim();
  };

  return (
    <Layout title={`${formatRoleName(role)} Roadmap`}>
      <Box 
        mb={4}
        sx={{
          background: 'linear-gradient(135deg, #1e3a8a 0%, #1e40af 100%)',
          borderRadius: 2,
          boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)',
          color: 'white',
          p: 4,
          position: 'relative',
          overflow: 'hidden',
          '&::before': {
            content: '""',
            position: 'absolute',
            top: 0,
            right: 0,
            bottom: 0,
            left: 0,
            background: 'radial-gradient(circle at 100% 0%, rgba(255,255,255,0.1) 0%, transparent 70%)',
            pointerEvents: 'none',
          },
        }}
      >
        <Grid container spacing={2} alignItems="center" justifyContent="space-between">
          <Grid item xs={12} md={8} component="div">
            <Typography 
              variant="h4" 
              component="h1" 
              sx={{
                fontWeight: 700,
                mb: 1,
                textShadow: '0 2px 4px rgba(0,0,0,0.1)',
                display: 'flex',
                alignItems: 'center',
                gap: 2
              }}
            >
              {formatRoleName(role)} Roadmap
              <Box 
                component="span" 
                sx={{
                  bgcolor: 'rgba(255, 255, 255, 0.15)',
                  px: 1.5,
                  py: 0.5,
                  borderRadius: 4,
                  fontSize: '0.8rem',
                  fontWeight: 500,
                  display: 'inline-flex',
                  alignItems: 'center',
                  gap: 1,
                  backdropFilter: 'blur(4px)'
                }}
              >
                <span style={{ opacity: 0.9 }}>📅</span>
                {timelineWeeks} week{timelineWeeks !== 1 ? 's' : ''}
              </Box>
            </Typography>
            
            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1.5, mt: 2, alignItems: 'center' }}>
              <Box 
                sx={{
                  bgcolor: 'rgba(255, 255, 255, 0.15)',
                  px: 2,
                  py: 0.75,
                  borderRadius: 4,
                  display: 'inline-flex',
                  alignItems: 'center',
                  gap: 1,
                  backdropFilter: 'blur(4px)'
                }}
              >
                <span style={{ opacity: 0.9 }}>👨‍💻</span>
                {formatExperience(experience)}
              </Box>
              
              {roadmap?.requiredSkills?.length ? (
                <Box 
                  sx={{
                    bgcolor: 'rgba(255, 255, 255, 0.15)',
                    px: 2,
                    py: 0.75,
                    borderRadius: 4,
                    display: 'inline-flex',
                    alignItems: 'center',
                    gap: 1,
                    backdropFilter: 'blur(4px)'
                  }}
                >
                  <span style={{ opacity: 0.9 }}>🛠️</span>
                  {roadmap.requiredSkills.length} key skills
                </Box>
              ) : null}
            </Box>
          </Grid>
          
          <Grid item xs={12} md={4} component="div" sx={{ mt: { xs: 3, md: 0 } }}>
            <Box sx={{ textAlign: { xs: 'left', md: 'right' } }}>
              <Typography 
                variant="subtitle2" 
                component="label" 
                htmlFor="timeline-weeks-select"
                sx={{
                  display: 'block',
                  mb: 1,
                  color: 'rgba(255, 255, 255, 0.9)',
                  fontWeight: 500,
                  fontSize: '0.75rem',
                  textTransform: 'uppercase',
                  letterSpacing: '0.05em'
                }}
              >
                Select Duration
              </Typography>
              <FormControl 
                variant="outlined" 
                size="small" 
                fullWidth
                sx={{
                  '& .MuiOutlinedInput-root': {
                    bgcolor: 'rgba(255, 255, 255, 0.1)',
                    border: '1px solid rgba(255, 255, 255, 0.2)',
                    borderRadius: 2,
                    transition: 'all 0.2s ease',
                    '&:hover': {
                      bgcolor: 'rgba(255, 255, 255, 0.15)',
                      '& .MuiOutlinedInput-notchedOutline': {
                        borderColor: 'rgba(255, 255, 255, 0.3)',
                      },
                    },
                    '&.Mui-focused': {
                      bgcolor: 'rgba(255, 255, 255, 0.15)',
                      '& .MuiOutlinedInput-notchedOutline': {
                        borderColor: 'rgba(255, 255, 255, 0.5)',
                        borderWidth: '1px',
                      },
                    },
                  },
                  '& .MuiSelect-select': {
                    py: '10px',
                    color: 'white',
                    fontWeight: 500,
                    display: 'flex',
                    alignItems: 'center',
                  },
                  '& .MuiSvgIcon-root': {
                    color: 'rgba(255, 255, 255, 0.7)',
                  },
                }}
              >
                <Select
                  id="timeline-weeks-select"
                  value={timelineWeeks}
                  onChange={handleTimelineWeeksChange}
                  disabled={isRegenerating}
                  MenuProps={{
                    PaperProps: {
                      sx: {
                        mt: 1,
                        borderRadius: 2,
                        boxShadow: '0 10px 25px -5px rgba(0, 0, 0, 0.2)',
                        '& .MuiMenuItem-root': {
                          px: 2,
                          py: '8px',
                          '&:hover': {
                            bgcolor: 'rgba(30, 58, 138, 0.1)',
                          },
                        },
                        '& .Mui-selected': {
                          bgcolor: 'rgba(30, 58, 138, 0.1)',
                          '&:hover': {
                            bgcolor: 'rgba(30, 58, 138, 0.15)',
                          },
                        },
                      },
                    },
                  }}
                >
                  {weekOptions.map((weeks) => (
                    <MenuItem key={weeks} value={weeks}>
                      <Box sx={{ 
                        display: 'flex', 
                        alignItems: 'center',
                        width: '100%',
                        justifyContent: 'space-between'
                      }}>
                        <span>{weeks} week{weeks !== 1 ? 's' : ''}</span>
                        {weeks === 12 && (
                          <span style={{
                            fontSize: '0.7rem',
                            color: '#4ade80',
                            backgroundColor: 'rgba(74, 222, 128, 0.15)',
                            padding: '2px 8px',
                            borderRadius: '10px',
                            fontWeight: 500
                          }}>
                            Recommended
                          </span>
                        )}
                      </Box>
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Box>
            
            {isRegenerating && (
              <Box 
                sx={{
                  mt: 1,
                  display: 'flex',
                  alignItems: 'center',
                  gap: 1,
                  color: 'rgba(255, 255, 255, 0.8)',
                  fontSize: '0.875rem',
                }}
              >
                <CircularProgress size={16} color="inherit" thickness={5} />
                Updating roadmap...
              </Box>
            )}
          </Grid>
        </Grid>
      </Box>

      {roadmap && <RoadmapCard roadmap={roadmap} />}
    </Layout>
  );
};

export default RoadmapPage;
