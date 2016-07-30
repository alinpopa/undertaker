package undertaker.service

trait Registry[K, V] {
  def reader: RegistryReader[K, V]
  def writer: RegistryWriter[K, V]
}
