import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';
import { ApiError } from '../models';

/**
 * Error Interceptor - Global HTTP error handling
 * Converts HTTP errors to ApiError format and logs them
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let apiError: ApiError;

      if (error.error instanceof ErrorEvent) {
        // Client-side or network error
        apiError = {
          status: 0,
          message: 'Network error occurred. Please check your connection.',
          details: error.error.message
        };
      } else {
        // Backend returned an unsuccessful response code
        apiError = {
          status: error.status,
          message: getErrorMessage(error.status),
          details: error.error?.message || error.message
        };
      }

      console.error('HTTP Error:', apiError);

      return throwError(() => apiError);
    })
  );
};

/**
 * Get user-friendly error message based on HTTP status code
 */
function getErrorMessage(status: number): string {
  switch (status) {
    case 400:
      return 'Invalid request. Please check your input.';
    case 404:
      return 'The requested resource was not found.';
    case 413:
      return 'File is too large. Maximum size is 10MB.';
    case 500:
      return 'Server error occurred. Please try again later.';
    case 503:
      return 'Service is temporarily unavailable. Please try again later.';
    default:
      return `An error occurred (${status}). Please try again.`;
  }
}
