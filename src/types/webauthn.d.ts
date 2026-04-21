// WebAuthn Type Definitions
interface PublicKeyCredential extends Credential {
  readonly rawId: ArrayBuffer
  readonly response: AuthenticatorResponse
  getClientExtensionResults(): AuthenticationExtensionsClientOutputs
}

interface AuthenticatorResponse {
  readonly clientDataJSON: ArrayBuffer
}

interface AuthenticatorAssertionResponse extends AuthenticatorResponse {
  readonly authenticatorData: ArrayBuffer
  readonly signature: ArrayBuffer
  readonly userHandle: ArrayBuffer | null
}

interface AuthenticatorAttestationResponse extends AuthenticatorResponse {
  readonly attestationObject: ArrayBuffer
}

interface PublicKeyCredentialCreationOptions {
  challenge: BufferSource
  rp: PublicKeyCredentialRpEntity
  user: PublicKeyCredentialUserEntity
  pubKeyCredParams: PublicKeyCredentialParameters[]
  timeout?: number
  excludeCredentials?: PublicKeyCredentialDescriptor[]
  authenticatorSelection?: AuthenticatorSelectionCriteria
  attestation?: AttestationConveyancePreference
  extensions?: AuthenticationExtensionsClientInputs
}

interface PublicKeyCredentialRequestOptions {
  challenge: BufferSource
  timeout?: number
  rpId?: string
  allowCredentials?: PublicKeyCredentialDescriptor[]
  userVerification?: UserVerificationRequirement
  extensions?: AuthenticationExtensionsClientInputs
}

interface CredentialsContainer {
  create(options?: CredentialCreationOptions): Promise<Credential | null>
  get(options?: CredentialRequestOptions): Promise<Credential | null>
  preventSilentAccess(): Promise<void>
  store(credential: Credential): Promise<Credential>
}

interface CredentialCreationOptions {
  publicKey?: PublicKeyCredentialCreationOptions
  signal?: AbortSignal
}

interface CredentialRequestOptions {
  publicKey?: PublicKeyCredentialRequestOptions
  mediation?: CredentialMediationRequirement
  signal?: AbortSignal
}

type AttestationConveyancePreference = 'none' | 'indirect' | 'direct' | 'enterprise'
type UserVerificationRequirement = 'required' | 'preferred' | 'discouraged'
type CredentialMediationRequirement = 'silent' | 'optional' | 'conditional' | 'required'

interface PublicKeyCredentialRpEntity {
  id?: string
  name: string
}

interface PublicKeyCredentialUserEntity {
  id: BufferSource
  name: string
  displayName: string
}

interface PublicKeyCredentialParameters {
  type: PublicKeyCredentialType
  alg: COSEAlgorithmIdentifier
}

interface PublicKeyCredentialDescriptor {
  type: PublicKeyCredentialType
  id: BufferSource
  transports?: AuthenticatorTransport[]
}

interface AuthenticatorSelectionCriteria {
  authenticatorAttachment?: AuthenticatorAttachment
  requireResidentKey?: boolean
  residentKey?: ResidentKeyRequirement
  userVerification?: UserVerificationRequirement
}

type PublicKeyCredentialType = 'public-key'
type COSEAlgorithmIdentifier = number
type AuthenticatorTransport = 'usb' | 'nfc' | 'ble' | 'internal'
type AuthenticatorAttachment = 'platform' | 'cross-platform'
type ResidentKeyRequirement = 'discouraged' | 'preferred' | 'required'

interface AuthenticationExtensionsClientInputs {
  [key: string]: any
}

interface AuthenticationExtensionsClientOutputs {
  [key: string]: any
}

declare global {
  interface Window {
    PublicKeyCredential: any
  }
  
  interface Navigator {
    credentials: CredentialsContainer
  }
}

export {}
