export interface ComplaintPayload {
  userId: string;
  category: 'Room Issue' | 'Service Issue' | 'Billing Issue' | 'Other' | string;
  bookingId: string;
  title: string;
  description: string;
  contactPreference: 'Call' | 'Email' | string;
}

export interface ComplaintRecord {
  id: string;
  userId: string;
  category: string;
  bookingId: string;
  title: string;
  description: string;
  contactPreference: string;
  status: 'Open' | 'In Progress' | 'Escalated' | 'Resolved' | 'Closed' | string;
  createdAt: string;
  expectedResolutionDate: string;
  editable: boolean;
  acknowledgementMessage: string;
  supportResponse: string;
  resolutionNotes: string;
  checkpointUpdates: ComplaintCheckpointUpdate[];
}

export interface ComplaintCheckpointUpdate {
  updatedAt: string;
  status: string;
  message: string;
  actorName: string;
}
