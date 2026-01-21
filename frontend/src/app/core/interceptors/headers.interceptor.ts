import { HttpInterceptorFn } from '@angular/common/http';

/**
 * Headers Interceptor - Adds common headers to all HTTP requests
 * Sets Content-Type and Accept headers for JSON communication
 */
export const headersInterceptor: HttpInterceptorFn = (req, next) => {
  // Skip adding Content-Type for multipart/form-data requests (file uploads)
  if (req.body instanceof FormData) {
    return next(req);
  }

  // Add common headers for JSON requests
  const modifiedReq = req.clone({
    setHeaders: {
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    }
  });

  return next(modifiedReq);
};
