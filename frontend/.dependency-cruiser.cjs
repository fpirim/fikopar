// Layering (bottom → top): types → lib → context/hooks → components → pages; orphans are knip's job.
module.exports = {
  forbidden: [
    {
      name: 'no-circular',
      comment: 'Circular dependency.',
      severity: 'error',
      from: {},
      to: { circular: true },
    },
    {
      name: 'types-pure',
      comment: 'src/types holds type definitions only.',
      severity: 'error',
      from: { path: '^src/types/' },
      to: { path: '^src/(pages|components|context|hooks|lib)/' },
    },
    {
      name: 'lib-no-upward',
      comment: 'src/lib is the base layer.',
      severity: 'error',
      from: { path: '^src/lib/' },
      to: { path: '^src/(pages|components|context|hooks)/' },
    },
    {
      name: 'context-hooks-no-pages',
      comment: 'context/hooks must not depend on pages.',
      severity: 'error',
      from: { path: '^src/(context|hooks)/' },
      to: { path: '^src/pages/' },
    },
    {
      name: 'components-no-pages',
      comment: 'Reusable components must not import pages.',
      severity: 'error',
      from: { path: '^src/components/' },
      to: { path: '^src/pages/' },
    },
    {
      name: 'mocks-dev-only',
      comment: 'MSW mocks load only from main.tsx (dev) and tests.',
      severity: 'error',
      from: { pathNot: '^src/(mocks/|main\\.tsx$|test/)' },
      to: { path: '^src/mocks/' },
    },
    {
      name: 'api-base-layer',
      comment: 'src/api is the transport layer.',
      severity: 'error',
      from: { path: '^src/api/' },
      to: { path: '^src/(pages|components|context|hooks)/' },
    },
    {
      name: 'no-test-in-prod',
      comment: 'Production code must not import tests.',
      severity: 'error',
      from: { pathNot: '\\.(test|spec)\\.[jt]sx?$' },
      to: { path: '\\.(test|spec)\\.[jt]sx?$' },
    },
  ],
  options: {
    doNotFollow: { path: 'node_modules' },
    tsConfig: { fileName: 'tsconfig.json' },
    tsPreCompilationDeps: true,
    exclude: { path: '(\\.test\\.|\\.spec\\.|/test/)' },
    enhancedResolveOptions: {
      extensions: ['.ts', '.tsx', '.js', '.jsx', '.json'],
    },
  },
}
