import React, { useState } from 'react';
import { Dialog } from '@mui/material';

interface ImageViewerProps {
  url: string;
  type: any;
  open: boolean;
  onClose: () => void;
}

const ImageViewer = ({ url, type, open, onClose }: ImageViewerProps) => {
  const [width, setWidth] = useState(0);
  const [height, setHeight] = useState(0);
  const [source, setSource] = useState(null);

  React.useEffect(() => {
    setSource(null);
    if (url) {
      const image = new Image();
      const handleLoad = () => {
        setSource(url);
        setHeight(image.height);
        setWidth(image.width);
        console.log(`width: ${image.width}, height: ${image.height}`);
      };
      image.addEventListener('load', handleLoad);
      image.src = url;
      return () => {
        image.removeEventListener('load', handleLoad);
      };
    }
  }, [url]);

  return (
    <Dialog
      maxWidth="xl"
      aria-labelledby="form-dialog-title"
      open={open}
      onClose={onClose}
      PaperProps={{
        style: { borderRadius: 10 },
      }}
    >
      {url === source && <img alt={url} src={url} style={{ width: width + 'px', height: height + 'px', maxHeight: '100%' }} />}
    </Dialog>
  );
};

export default ImageViewer;
