import Document, {
  Html,
  Head,
  Main,
  NextScript,
  DocumentContext,
  DocumentInitialProps,
} from 'next/document';
import { ServerStyleSheet } from 'styled-components';

class MyDocument extends Document {
  static async getInitialProps(
    ctx: DocumentContext
  ): Promise<DocumentInitialProps> {
    const sheet = new ServerStyleSheet();
    const originalRenderPage = ctx.renderPage;

    try {
      ctx.renderPage = () =>
        originalRenderPage({
          enhanceApp: (App) => (props) =>
            sheet.collectStyles(<App {...props} />),
        });

      const initialProps = await Document.getInitialProps(ctx);
      return {
        ...initialProps,
        styles: [
          ...(Array.isArray(initialProps.styles) ? initialProps.styles : []),
          sheet.getStyleElement(),
        ],
      };
    } finally {
      sheet.seal();
    }
  }

  render() {
    return (
      <Html lang="en" className="scroll-smooth">
        <Head>
          <meta charSet="utf-8" />
          <meta name="theme-color" content="#ffffff" />
          <meta name="description" content="Your personal career growth companion" />
          
          {/* Favicon and app icons */}
          <link rel="icon" href="/favicon.ico" sizes="any" />
          <link rel="icon" href="/favicon.svg" type="image/svg+xml" />
          <link rel="apple-touch-icon" href="/apple-touch-icon.png" />
          <link rel="manifest" href="/site.webmanifest" />
          
          {/* Google Fonts */}
          <link rel="preconnect" href="https://fonts.googleapis.com" />
          <link rel="preconnect" href="https://fonts.gstatic.com" crossOrigin="anonymous" />
          <link
            href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@400;500&display=swap"
            rel="stylesheet"
          />
          
          {/* Critical CSS */}
          <style
            dangerouslySetInnerHTML={{
              __html: `
                /* Prevent layout shift on page load */
                html {
                  scroll-behavior: smooth;
                }
                
                /* Custom scrollbar */
                ::-webkit-scrollbar {
                  width: 8px;
                  height: 8px;
                }
                
                ::-webkit-scrollbar-track {
                  background: #f1f5f9;
                }
                
                ::-webkit-scrollbar-thumb {
                  background: #cbd5e1;
                  border-radius: 4px;
                }
                
                ::-webkit-scrollbar-thumb:hover {
                  background: #94a3b8;
                }
                
                /* Remove focus styles for mouse users */
                :focus:not(:focus-visible) {
                  outline: none;
                }
                
                /* Focus styles for keyboard navigation */
                :focus-visible {
                  outline: 2px solid #0ea5e9;
                  outline-offset: 2px;
                }
              `,
            }}
          />
        </Head>
        <body className="bg-white text-gray-900 antialiased transition-colors duration-200 dark:bg-gray-900 dark:text-white">
          <Main />
          <NextScript />
          
          {/* Add your analytics scripts here */}
          <script
            async
            src="https://www.googletagmanager.com/gtag/js?id=G-XXXXXXXXXX"
          />
          <script
            dangerouslySetInnerHTML={{
              __html: `
                window.dataLayer = window.dataLayer || [];
                function gtag(){dataLayer.push(arguments);}
                gtag('js', new Date());
                gtag('config', 'G-XXXXXXXXXX');
              `,
            }}
          />
        </body>
      </Html>
    );
  }
}

export default MyDocument;
