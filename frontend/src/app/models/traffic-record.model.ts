export interface TrafficRecord {
  id: number;
  sessionId: string;
  method: string;
  path: string;
  requestHeaders: { [key: string]: string };
  requestBody: string;
  statusCode: number;
  responseHeaders: { [key: string]: string };
  responseBody: string;
  timestamp: string;
  createdAt: string;
}
