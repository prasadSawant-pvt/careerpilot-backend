import React from 'react';
import { Disclosure } from '@headlessui/react';
import { ChevronDownIcon } from '@heroicons/react/24/outline';
import { InterviewQuestion } from '../types';

interface QuestionAccordionProps {
  questions: InterviewQuestion[];
}

const QuestionAccordion: React.FC<QuestionAccordionProps> = ({ questions }) => {
  return (
    <div className="mb-8">
      <h3 className="text-xl font-semibold text-gray-900 mb-4">
        Interview Questions
      </h3>
      <div className="space-y-3">
        {questions.map((item, index) => (
          <Disclosure key={index}>
            {({ open }) => (
              <div className="border border-gray-200 rounded-lg overflow-hidden">
                <Disclosure.Button className="w-full px-4 py-3 text-left text-sm font-medium text-gray-900 bg-gray-50 hover:bg-gray-100 focus:outline-none focus-visible:ring focus-visible:ring-primary-500 focus-visible:ring-opacity-75 flex justify-between items-center">
                  <span>{item.question}</span>
                  <ChevronDownIcon className={`${open ? 'transform rotate-180' : ''} w-5 h-5 text-gray-500`} />
                </Disclosure.Button>
                <Disclosure.Panel className="px-4 pt-3 pb-4 text-sm text-gray-700 bg-white">
                  {item.answer}
                </Disclosure.Panel>
              </div>
            )}
          </Disclosure>
        ))}
      </div>
    </div>
  );
};

export default QuestionAccordion;
