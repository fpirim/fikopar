import js from '@eslint/js'
import globals from 'globals'
import tseslint from 'typescript-eslint'
import react from 'eslint-plugin-react'
import reactHooks from 'eslint-plugin-react-hooks'
import reactRefresh from 'eslint-plugin-react-refresh'
import prettier from 'eslint-config-prettier'

export default tseslint.config(
  {
    ignores: ['dist', 'coverage', 'playwright-report', 'test-results', 'node_modules'],
  },
  js.configs.recommended,
  ...tseslint.configs.recommended,
  {
    files: ['**/*.{ts,tsx}'],
    languageOptions: {
      ecmaVersion: 2022,
      globals: { ...globals.browser },
      parserOptions: {
        ecmaFeatures: { jsx: true },
      },
    },
    settings: {
      react: { version: 'detect' },
    },
    plugins: {
      react,
      'react-hooks': reactHooks,
      'react-refresh': reactRefresh,
    },
    rules: {
      ...react.configs.recommended.rules,
      ...react.configs['jsx-runtime'].rules,
      ...reactHooks.configs.recommended.rules,
      'react-refresh/only-export-components': ['warn', { allowConstantExport: true }],
      // The codebase intentionally uses `any` for axios error shapes and grid rows.
      '@typescript-eslint/no-explicit-any': 'off',
      // React Compiler readiness: real refactor candidates, kept as warnings until addressed.
      'react-hooks/refs': 'warn',
      'react-hooks/static-components': 'warn',
    },
  },
  // Vitest/Playwright test files: allow node + test globals.
  {
    files: ['**/*.{test,spec}.{ts,tsx}', 'e2e/**/*.ts', 'src/test/**/*.ts'],
    languageOptions: {
      globals: { ...globals.node },
    },
  },
  // CommonJS config files (e.g. .dependency-cruiser.cjs) run under Node.
  {
    files: ['**/*.cjs'],
    languageOptions: {
      sourceType: 'commonjs',
      globals: { ...globals.node },
    },
  },
  prettier,
)
