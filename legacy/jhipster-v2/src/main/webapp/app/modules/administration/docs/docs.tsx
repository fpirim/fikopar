import './docs.scss';

import React from 'react';

const DocsPage = () => (
  <div
    style={{
      height: 'calc(100vh - 150px)',
    }}
  >
    <iframe
      src="../swagger-ui/index.html"
      width="100%"
      height="100%"
      title="Swagger UI"
      seamless
      style={{ border: 'none' }}
      data-cy="swagger-frame"
    />
  </div>
);

export default DocsPage;
