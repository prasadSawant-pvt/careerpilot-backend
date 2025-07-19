import React from 'react';
import { Button } from './ui/button';

type ErrorFallbackProps = {
  error: Error;
  resetErrorBoundary: () => void;
  className?: string;
};

export function ErrorFallback({
  error,
  resetErrorBoundary,
  className = '',
}: ErrorFallbackProps) {
  return (
    <div
      className={`flex min-h-screen flex-col items-center justify-center p-4 text-center ${className}`}
      role="alert"
    >
      <div className="max-w-md space-y-4">
        <div className="text-6xl">😕</div>
        <h2 className="text-2xl font-bold text-gray-900 dark:text-white">
          Something went wrong
        </h2>
        <p className="text-gray-600 dark:text-gray-400">
          We're sorry, but an unexpected error occurred. Our team has been
          notified and we're working on fixing it.
        </p>
        <div className="rounded-lg bg-red-50 p-4 text-left text-sm text-red-700 dark:bg-red-900/20 dark:text-red-200">
          <p className="font-medium">Error details:</p>
          <pre className="mt-1 overflow-x-auto rounded bg-white/50 p-2 text-xs dark:bg-black/20">
            {error.message || 'Unknown error'}
          </pre>
        </div>
        <div className="flex flex-col space-y-2 pt-4 sm:flex-row sm:justify-center sm:space-x-3 sm:space-y-0">
          <Button
            onClick={resetErrorBoundary}
            className="w-full sm:w-auto"
            variant="default"
          >
            Try again
          </Button>
          <Button
            asChild
            variant="outline"
            className="w-full sm:w-auto"
          >
            <a href="/">Go to home</a>
          </Button>
        </div>
      </div>
    </div>
  );
}
