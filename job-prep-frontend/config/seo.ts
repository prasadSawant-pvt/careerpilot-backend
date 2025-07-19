import { DefaultSeoProps } from 'next-seo';

export const SITE_NAME = 'CareerPilot';
export const SITE_URL = process.env.NEXT_PUBLIC_SITE_URL || 'https://careerpilot.app';
export const SITE_DESCRIPTION = 'Your personal career growth companion. Get personalized learning paths, interview preparation, and career guidance.';

export const SEO: DefaultSeoProps = {
  titleTemplate: `%s | ${SITE_NAME}`,
  defaultTitle: SITE_NAME,
  description: SITE_DESCRIPTION,
  openGraph: {
    type: 'website',
    locale: 'en_US',
    url: SITE_URL,
    siteName: SITE_NAME,
    title: SITE_NAME,
    description: SITE_DESCRIPTION,
    images: [
      {
        url: `${SITE_URL}/images/og-image.jpg`,
        width: 1200,
        height: 630,
        alt: SITE_NAME,
        type: 'image/jpeg',
      },
    ],
  },
  twitter: {
    handle: '@careerpilot',
    site: '@careerpilot',
    cardType: 'summary_large_image',
  },
  additionalMetaTags: [
    {
      name: 'viewport',
      content: 'width=device-width, initial-scale=1',
    },
    {
      name: 'theme-color',
      content: '#2563eb',
    },
    {
      name: 'apple-mobile-web-app-capable',
      content: 'yes',
    },
    {
      name: 'apple-mobile-web-app-status-bar-style',
      content: 'default',
    },
  ],
  additionalLinkTags: [
    {
      rel: 'icon',
      href: '/favicon.ico',
    },
    {
      rel: 'apple-touch-icon',
      href: '/apple-touch-icon.png',
      sizes: '180x180',
    },
    {
      rel: 'manifest',
      href: '/site.webmanifest',
    },
  ],
};

// Helper function to generate page-specific SEO props
export const getPageSeo = ({
  title,
  description,
  path = '',
  image = '/images/og-image.jpg',
}: {
  title: string;
  description?: string;
  path?: string;
  image?: string;
}) => ({
  title,
  description: description || SITE_DESCRIPTION,
  openGraph: {
    url: `${SITE_URL}${path}`,
    title,
    description: description || SITE_DESCRIPTION,
    images: [
      {
        url: image.startsWith('http') ? image : `${SITE_URL}${image}`,
        width: 1200,
        height: 630,
        alt: title,
      },
    ],
  },
});
