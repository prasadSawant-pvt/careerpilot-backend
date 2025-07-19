/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  images: {
    domains: ['images.unsplash.com'],
  },
  // Disable Turbopack for now to avoid compatibility issues
  experimental: {
    // Remove turbo configuration as it's not needed
  },
};

module.exports = nextConfig;
