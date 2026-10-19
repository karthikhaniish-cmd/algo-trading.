/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        navy: {
          950: '#070C18',
          900: '#0B132B',
          850: '#101B38',
          800: '#142244',
          750: '#1B2C56',
          700: '#23386C',
          600: '#2F4885'
        },
        cyan: {
          accent: '#00E5FF',
          glow: '#38BDF8',
          deep: '#0284C7'
        },
        emerald: {
          trading: '#00E676',
          muted: '#10B981'
        },
        ruby: {
          trading: '#FF5252',
          muted: '#EF4444'
        }
      }
    },
  },
  plugins: [],
}
