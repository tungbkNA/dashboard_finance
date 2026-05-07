// project-settings.ts — TypeScript types for Feature 002

export type StatusProject = 'OPEN' | 'INPROGRESS' | 'PENDING' | 'DONE' | 'CLOSE'
export type StatusContract = 'NO_CONTRACT' | 'HAS_CONTRACT'

// ---- ProjectType ----

export interface ProjectTypeResponse {
  id: string
  key: string
  value: string
}

export interface ProjectTypeRequest {
  key: string
  value: string
}

// ---- Customer ----

export interface CustomerResponse {
  id: string
  customerCode: string
  customerName: string
}

export interface CustomerRequest {
  customerCode: string
  customerName: string
}

// ---- Project ----

export interface ProjectResponse {
  id: string
  projectCode: string
  projectName: string
  customerId: string
  customerName: string
  projectTypeId: string
  projectTypeName: string
  price: number
  statusContract: StatusContract
  statusProject: StatusProject
  monthStart: string
  monthEnd: string
  createdAt: string
  updatedAt: string
}

export interface ProjectRequest {
  projectCode: string
  projectName: string
  customerId: string
  projectTypeId: string
  price: number
  statusContract: StatusContract
  statusProject: StatusProject
  monthStart: string
  monthEnd: string
}

// ---- Delete Check ----

export interface DeleteCheckResponse {
  inUse: boolean
  usageCount: number
}
