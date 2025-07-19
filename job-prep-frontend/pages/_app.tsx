import { useState, useEffect, type ReactNode } from 'react';
import type { AppProps } from 'next/app';
import { useRouter } from 'next/router';
import { ThemeProvider } from 'next-themes';
import { DefaultSeo } from 'next-seo';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';
import { ErrorBoundary } from 'react-error-boundary';
import { Toaster } from 'react-hot-toast';
import { Theme } from '@radix-ui/themes';
import { GlobalStyle } from '../styles/global-styles';
import { ErrorFallback } from '../components/ErrorFallback';
import { PageLoader } from '../components/PageLoader';
import { AuthProvider } from '../contexts/AuthContext';
import { SEO } from '../config/seo';
import '@radix-ui/themes/styles.css';
import '../styles/globals.css';

// Create a client
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: 1,
      staleTime: 5 * 60 * 1000, // 5 minutes
    },
  },
});

// Error boundary fallback component
const ErrorBoundaryFallback = ({
  error,
  resetErrorBoundary,
}: {
  error: Error;
  resetErrorBoundary: () => void;
}) => (
  <ErrorFallback
    error={error}
    resetErrorBoundary={resetErrorBoundary}
    className="min-h-screen"
  />
);

type AppPropsWithLayout = AppProps & {
  Component: AppProps['Component'] & {
    getLayout?: (page: ReactNode) => ReactNode;
  };
};

function MyApp({ Component, pageProps }: AppPropsWithLayout) {
  const router = useRouter();
  const [pageLoading, setPageLoading] = useState(false);
  const getLayout = Component.getLayout ?? ((page) => page);

  useEffect(() => {
    const handleStart = () => setPageLoading(true);
    const handleComplete = () => setPageLoading(false);

    router.events.on('routeChangeStart', handleStart);
    router.events.on('routeChangeComplete', handleComplete);
    router.events.on('routeChangeError', handleComplete);

    return () => {
      router.events.off('routeChangeStart', handleStart);
      router.events.off('routeChangeComplete', handleComplete);
      router.events.off('routeChangeError', handleComplete);
    };
  }, [router.events]);

  // Add a class to the body when the page is loading
  useEffect(() => {
    if (pageLoading) {
      document.body.classList.add('page-loading');
    } else {
      document.body.classList.remove('page-loading');
    }
  }, [pageLoading]);

  return (
    <ErrorBoundary
      FallbackComponent={ErrorBoundaryFallback}
      onReset={() => {
        // Reset the state of your app so the error doesn't happen again
        router.push('/');
      }}
    >
      <QueryClientProvider client={queryClient}>
        <ThemeProvider
          attribute="class"
          defaultTheme="system"
          enableSystem
          disableTransitionOnChange
        >
          <Theme>
            <AuthProvider>
              <GlobalStyle />
              <DefaultSeo {...SEO} />
              <div id="skip-nav">
                <a
                  href="#main-content"
                  className="sr-only focus:not-sr-only focus:fixed focus:top-4 focus:left-4 focus:z-50 focus:bg-white focus:px-4 focus:py-2 focus:border-2 focus:border-blue-500 focus:rounded focus:font-medium"
                >
                  Skip to content
                </a>
              </div>
              {pageLoading ? (
                <PageLoader />
              ) : (
                getLayout(<Component {...pageProps} />)
              )}
              <Toaster
                position="bottom-right"
                toastOptions={{
                  duration: 5000,
                  style: {
                    background: 'hsl(var(--background))',
                    color: 'hsl(var(--foreground))',
                    border: '1px solid hsl(var(--border))',
                    borderRadius: 'var(--radius)',
                    padding: '12px 16px',
                    fontSize: '0.875rem',
                    lineHeight: '1.25rem',
                    boxShadow:
                      '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)',
                  },
                }}
              />
            </AuthProvider>
          </Theme>
        </ThemeProvider>
        {process.env.NODE_ENV === 'development' && (
          <ReactQueryDevtools initialIsOpen={false} position="bottom-right" as="div" />
        )}
      </QueryClientProvider>
    </ErrorBoundary>
  );
}

export default MyApp;
