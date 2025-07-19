import { useState, useCallback } from 'react';
import type { ApiResponse, ApiError } from '@/types/api';

interface UseApiOptions<T> {
  onSuccess?: (data: T) => void;
  onError?: (error: ApiError) => void;
}

export function useApi<T>() {
  const [data, setData] = useState<T | null>(null);
  const [error, setError] = useState<ApiError | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [status, setStatus] = useState<number | null>(null);

  const execute = useCallback(
    async (
      apiCall: () => Promise<ApiResponse<T>>,
      options: UseApiOptions<T> = {}
    ): Promise<ApiResponse<T> | undefined> => {
      setIsLoading(true);
      setError(null);

      try {
        const response = await apiCall();
        setData(response.data);
        setStatus(response.status);
        
        if (options.onSuccess) {
          options.onSuccess(response.data);
        }
        
        return response;
      } catch (err) {
        const apiError = err as ApiError;
        setError(apiError);
        setStatus(apiError.status || 500);
        
        if (options.onError) {
          options.onError(apiError);
        }
        
        console.error('API Error:', apiError);
      } finally {
        setIsLoading(false);
      }
    },
    []
  );

  return {
    data,
    error,
    isLoading,
    status,
    execute,
    reset: () => {
      setData(null);
      setError(null);
      setStatus(null);
    },
  };
}

// Example usage:
/*
const MyComponent = () => {
  const { data, error, isLoading, execute } = useApi<{ roles: Role[] }>();

  const fetchRoles = useCallback(() => {
    execute(ApiService.getRoles(), {
      onSuccess: (data) => console.log('Fetched roles:', data),
      onError: (error) => console.error('Failed to fetch roles:', error)
    });
  }, [execute]);

  useEffect(() => {
    fetchRoles();
  }, [fetchRoles]);

  if (isLoading) return <div>Loading...</div>;
  if (error) return <div>Error: {error.message}</div>;

  return (
    <div>
      {data?.roles.map(role => (
        <div key={role.id}>{role.name}</div>
      ))}
    </div>
  );
};
*/
