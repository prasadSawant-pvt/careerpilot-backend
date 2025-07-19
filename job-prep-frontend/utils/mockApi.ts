import apiMap from './sample-api-map';

type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH';

interface MockApiOptions {
  params?: Record<string, string>;
  query?: Record<string, string>;
  body?: any;
}

export const mockApi = async <T>(
  path: string,
  method: HttpMethod = 'GET',
  options: MockApiOptions = {}
): Promise<{ data: T; status: number }> => {
  // Simulate network delay
  await new Promise(resolve => setTimeout(resolve, 300));

  // Find the matching endpoint configuration
  const endpoint = Object.entries(apiMap).find(([endpointPath, config]) => {
    // Handle dynamic routes (e.g., /api/roadmap/:role/:experience)
    const pathPattern = endpointPath.replace(/:[^/]+/g, '([^/]+)');
    const regex = new RegExp(`^${pathPattern}$`);
    return regex.test(path) && config.method === method;
  });

  if (!endpoint) {
    return {
      data: { message: 'Endpoint not found' } as any,
      status: 404,
    };
  }

  const [endpointPath, config] = endpoint;
  
  // Extract path parameters
  const pathParams: Record<string, string> = {};
  const pathSegments = endpointPath.split('/');
  const requestSegments = path.split('/');
  
  pathSegments.forEach((segment, index) => {
    if (segment.startsWith(':')) {
      const paramName = segment.slice(1);
      pathParams[paramName] = requestSegments[index];
    }
  });

  // Get the response from the mock data
  let response = JSON.parse(JSON.stringify(config.response));
  
  // If there's a transform function, use it to modify the response
  if (typeof response === 'function') {
    response = response({
      params: pathParams,
      query: options.query || {},
      body: options.body,
    });
  }

  return {
    data: response as T,
    status: 200,
  };
};

// Example usage:
// const { data } = await mockApi<{ roles: string[] }>('/api/roles');
// const { data: roadmap } = await mockApi<RoadmapResponse>('/api/roadmap/backend/junior');
