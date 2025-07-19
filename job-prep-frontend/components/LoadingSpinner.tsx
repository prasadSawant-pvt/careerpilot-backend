import React from 'react';
import { cn } from '../lib/utils';

interface LoadingSpinnerProps {
  className?: string;
  size?: 'sm' | 'md' | 'lg';
  label?: string;
}

const sizeClasses = {
  sm: 'h-5 w-5 border-2',
  md: 'h-8 w-8 border-2',
  lg: 'h-12 w-12 border-[3px]',
};

const LoadingSpinner: React.FC<LoadingSpinnerProps> = ({
  className = '',
  size = 'md',
  label = 'Loading...',
}) => {
  const sizeClass = sizeClasses[size] || sizeClasses.md;
  
  return (
    <div 
      className={cn(
        'flex flex-col items-center justify-center space-y-3',
        className
      )}
      role="status"
      aria-live="polite"
      aria-label={label}
    >
      <div 
        className={cn(
          'animate-spin rounded-full border-t-transparent border-b-transparent border-blue-600',
          sizeClass
        )}
      >
        <span className="sr-only">{label}</span>
      </div>
      {label && (
        <span className="text-sm text-gray-600">{label}</span>
      )}
    </div>
  );
};

export default LoadingSpinner;
