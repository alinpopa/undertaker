package undertaker.service

trait RegistryWriter[K, V] {
  def register(key: K, value: V): V
  def unregister(key: K): Option[V]
}
