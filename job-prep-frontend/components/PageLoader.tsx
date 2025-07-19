import React from 'react';

type PageLoaderProps = {
  className?: string;
  size?: 'sm' | 'md' | 'lg';
  text?: string;
};

export function PageLoader({
  className = '',
  size = 'md',
  text = 'Loading...',
}: PageLoaderProps) {
  const sizeClasses = {
    sm: 'h-4 w-4',
    md: 'h-8 w-8',
    lg: 'h-12 w-12',
  };

  return (
    <div
      className={`flex min-h-[50vh] flex-col items-center justify-center ${className}`}
    >
      <div className="flex flex-col items-center space-y-4">
        <div
          className={`${sizeClasses[size]} animate-spin rounded-full border-4 border-solid border-primary-500 border-t-transparent`}
          role="status"
          aria-label="Loading"
        >
          <span className="sr-only">{text}</span>
        </div>
        {text && (
          <p className="text-sm font-medium text-gray-600 dark:text-gray-400">
            {text}
          </p>
        )}
      </div>
    </div>
  );
}
