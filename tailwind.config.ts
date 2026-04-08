import type { Config } from 'tailwindcss';

export default {
  content: ['./index.html', './**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        stripe: {
          purple: '#533afd',
          'purple-hover': '#4434d4',
          'purple-deep': '#2e2b8c',
          'purple-light': '#b9b9f9',
          'purple-mid': '#665efd',
          navy: '#061b31',
          'dark-navy': '#0d253d',
          'brand-dark': '#1c1e54',
          ruby: '#ea2261',
          magenta: '#f96bee',
          'magenta-light': '#ffd7ef',
          lemon: '#9b6829',
          green: '#15be53',
          'green-text': '#108c3d',
        },
        neutral: {
          heading: '#061b31',
          label: '#273951',
          body: '#64748d',
          border: '#e5edf5',
          'border-purple': '#b9b9f9',
          'border-soft': '#d6d9fc',
        },
      },
      fontFamily: {
        sans: ['Inter', 'SF Pro Display', 'system-ui', 'sans-serif'],
        mono: ['Source Code Pro', 'SFMono-Regular', 'monospace'],
      },
      fontSize: {
        'display-hero': ['3.5rem', { lineHeight: '1.03', letterSpacing: '-0.025em', fontWeight: '300' }],
        'display-large': ['3rem', { lineHeight: '1.15', letterSpacing: '-0.02em', fontWeight: '300' }],
        'section-heading': ['2rem', { lineHeight: '1.10', letterSpacing: '-0.02em', fontWeight: '300' }],
        'sub-heading-lg': ['1.63rem', { lineHeight: '1.12', letterSpacing: '-0.01em', fontWeight: '300' }],
        'sub-heading': ['1.38rem', { lineHeight: '1.10', letterSpacing: '-0.01em', fontWeight: '300' }],
        'body-lg': ['1.13rem', { lineHeight: '1.40', fontWeight: '300' }],
        'body': ['1rem', { lineHeight: '1.50', fontWeight: '300' }],
        'button': ['1rem', { lineHeight: '1.00', fontWeight: '400' }],
        'button-sm': ['0.88rem', { lineHeight: '1.00', fontWeight: '400' }],
        'caption': ['0.81rem', { lineHeight: '1.33', fontWeight: '400' }],
        'caption-sm': ['0.75rem', { lineHeight: '1.33', fontWeight: '300' }],
        'tabular': ['0.75rem', { lineHeight: '1.33', letterSpacing: '-0.03em', fontVariantNumeric: 'tabular-nums' }],
      },
      boxShadow: {
        'stripe-sm': 'rgba(23,23,23,0.06) 0px 3px 6px',
        'stripe-md': 'rgba(23,23,23,0.08) 0px 15px 35px',
        'stripe-lg': 'rgba(50,50,93,0.25) 0px 30px 45px -30px, rgba(0,0,0,0.1) 0px 18px 36px -18px',
        'stripe-xl': 'rgba(3,3,39,0.25) 0px 14px 21px -14px, rgba(0,0,0,0.1) 0px 8px 17px -8px',
        'ring-focus': '0 0 0 2px #533afd',
      },
      borderRadius: {
        'stripe': '4px',
        'stripe-comfortable': '5px',
        'stripe-relaxed': '6px',
        'stripe-large': '8px',
      },
      spacing: {
        '0.5': '2px',
        '1.5': '6px',
        '2.5': '10px',
        '3.5': '14px',
        '4.5': '18px',
      },
    },
  },
  plugins: [],
} satisfies Config;
